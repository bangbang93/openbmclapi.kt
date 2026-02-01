package com.bangbang93.openbmclapi.agent.routes

import com.bangbang93.openbmclapi.agent.config.ClusterConfig
import com.bangbang93.openbmclapi.agent.model.Counters
import com.bangbang93.openbmclapi.agent.storage.IStorage
import com.bangbang93.openbmclapi.agent.util.HashUtil
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.response.respondOutputStream
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.plusAssign

private val logger = KotlinLogging.logger {}

// 最大带宽测量大小（MB）
private const val MAX_MEASURE_SIZE_MB = 200

@OptIn(ExperimentalAtomicApi::class)
fun Route.clusterRoutes(config: ClusterConfig, storage: IStorage, counters: Counters) {
  get("/download/{hash}") {
    val hash =
        call.parameters["hash"]?.lowercase()
            ?: run {
              call.respond(HttpStatusCode.BadRequest, "Missing hash parameter")
              return@get
            }

    val query =
        call.request.queryParameters.entries().associate {
          it.key to (it.value.firstOrNull() ?: "")
        }

    val signValid = HashUtil.checkSign(hash, config.clusterSecret, query)
    if (!signValid) {
      call.respond(HttpStatusCode.Forbidden, "invalid sign")
      return@get
    }

    val hashPath = HashUtil.hashToFilename(hash)
    if (!storage.exists(hashPath)) {
      call.respond(HttpStatusCode.NotFound, "File not found")
      return@get
    }

    call.response.header("x-bmclapi-hash", hash)
    val name = call.request.queryParameters["name"]

    try {
      val result = storage.serveFile(hashPath, call, name)
      counters.bytes += result.bytes
      counters.hits += result.hits
    } catch (e: Exception) {
      logger.error(e) { "Error serving file: $hash" }
      call.respond(HttpStatusCode.InternalServerError, "Error serving file")
    }
  }

  get("/measure/{size}") {
    val query =
        call.request.queryParameters.entries().associate {
          it.key to (it.value.firstOrNull() ?: "")
        }

    val path = "/measure/${call.parameters["size"]}"
    val isSignValid = HashUtil.checkSign(path, config.clusterSecret, query)
    if (!isSignValid) {
      call.respond(HttpStatusCode.Forbidden)
      return@get
    }

    val size =
        call.parameters["size"]?.toIntOrNull()
            ?: run {
              call.respond(HttpStatusCode.BadRequest)
              return@get
            }

    if (size > MAX_MEASURE_SIZE_MB) {
      call.respond(HttpStatusCode.BadRequest)
      return@get
    }

    val buffer =
        ByteArray(1024 * 1024) {
          (it % 4).let { idx ->
            when (idx) {
              0 -> 0x00
              1 -> 0x66
              2 -> 0xcc.toByte()
              3 -> 0xff.toByte()
              else -> 0
            }
          }
        }

    call.response.header(HttpHeaders.ContentLength, (size * 1024 * 1024).toString())

    call.respondOutputStream(contentType = ContentType.Application.OctetStream) {
      repeat(size) { write(buffer) }
    }
  }
}
