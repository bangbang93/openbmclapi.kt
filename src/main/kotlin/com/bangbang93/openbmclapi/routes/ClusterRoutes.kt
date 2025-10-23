package com.bangbang93.openbmclapi.routes

import com.bangbang93.openbmclapi.config.ClusterConfig
import com.bangbang93.openbmclapi.model.Counters
import com.bangbang93.openbmclapi.storage.IStorage
import com.bangbang93.openbmclapi.util.HashUtil
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("ClusterRoutes")

fun Route.clusterRoutes(config: ClusterConfig, storage: IStorage, counters: Counters) {
    get("/download/{hash}") {
        val hash = call.parameters["hash"]?.lowercase() ?: run {
            call.respond(HttpStatusCode.BadRequest, "Missing hash parameter")
            return@get
        }

        val query = call.request.queryParameters.entries()
            .associate { it.key to (it.value.firstOrNull() ?: "") }

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
            logger.error("Error serving file: $hash", e)
            call.respond(HttpStatusCode.InternalServerError, "Error serving file")
        }
    }

    get("/measure/{size}") {
        val query = call.request.queryParameters.entries()
            .associate { it.key to (it.value.firstOrNull() ?: "") }
        
        val path = "/measure/${call.parameters["size"]}"
        val isSignValid = HashUtil.checkSign(path, config.clusterSecret, query)
        if (!isSignValid) {
            call.respond(HttpStatusCode.Forbidden)
            return@get
        }

        val size = call.parameters["size"]?.toIntOrNull() ?: run {
            call.respond(HttpStatusCode.BadRequest)
            return@get
        }

        if (size > 200) {
            call.respond(HttpStatusCode.BadRequest)
            return@get
        }

        val buffer = ByteArray(1024 * 1024) { (it % 4).let { idx ->
            when(idx) {
                0 -> 0x00
                1 -> 0x66
                2 -> 0xcc.toByte()
                3 -> 0xff.toByte()
                else -> 0
            }
        } }

        call.response.header(HttpHeaders.ContentLength, (size * 1024 * 1024).toString())
        
        call.respondOutputStream(contentType = ContentType.Application.OctetStream) {
            repeat(size) {
                write(buffer)
            }
        }
    }
}
