package com.bangbang93.openbmclapi.agent.service

import com.bangbang93.openbmclapi.agent.config.AGENT_PROTOCOL_VERSION
import com.bangbang93.openbmclapi.agent.config.ClusterConfig
import com.bangbang93.openbmclapi.agent.model.CertificateResponse
import com.bangbang93.openbmclapi.agent.model.EnableRequest
import com.bangbang93.openbmclapi.agent.model.FileInfo
import com.bangbang93.openbmclapi.agent.model.FileList
import com.bangbang93.openbmclapi.agent.model.OpenbmclapiAgentConfiguration
import com.bangbang93.openbmclapi.agent.model.SyncConfig
import com.bangbang93.openbmclapi.agent.nat.NatService
import com.bangbang93.openbmclapi.agent.storage.IStorage
import com.bangbang93.openbmclapi.agent.util.HashUtil
import com.bangbang93.openbmclapi.agent.util.emitAck
import com.github.avrokotlin.avro4k.Avro
import com.github.luben.zstd.Zstd.decompress
import com.github.michaelbull.retry.policy.stopAtAttempts
import com.github.michaelbull.retry.retry
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.socket.client.IO
import io.socket.client.Socket
import java.net.URI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.serialization.decodeFromByteArray
import org.koin.core.annotation.Single

private val logger = KotlinLogging.logger {}

@Single
class ClusterService(
    private val config: ClusterConfig,
    private val storage: IStorage,
    private val tokenManager: TokenManager,
    private val httpClient: HttpClient,
    private val natService: NatService,
) : CoroutineScope by CoroutineScope(Dispatchers.Default) {
  lateinit var socket: Socket
  var isEnabled = false
  var wantEnable = false

  suspend fun getFileList(lastModified: Long? = null): FileList {
    val response =
        httpClient.get("${config.clusterBmclapi}/openbmclapi/files") {
          header(HttpHeaders.Authorization, "Bearer ${tokenManager.getToken()}")
          if (lastModified != null) {
            parameter("lastModified", lastModified)
          }
        }

    if (response.status == HttpStatusCode.NoContent) {
      return FileList(emptyList())
    }

    val body = response.body<ByteArray>()
    return Avro.decodeFromByteArray<FileList>(decompress(body, 20 * 1024 * 1024))
  }

  suspend fun getConfiguration(): OpenbmclapiAgentConfiguration =
      httpClient
          .get("${config.clusterBmclapi}/openbmclapi/configuration") {
            header(HttpHeaders.Authorization, "Bearer ${tokenManager.getToken()}")
          }
          .body()

  suspend fun syncFiles(
      fileList: FileList,
      syncConfig: SyncConfig,
  ) {
    val storageReady = storage.check()
    if (!storageReady) {
      throw Exception("Storage is not ready")
    }

    logger.info { "Checking for missing files" }
    val missingFiles = storage.getMissingFiles(fileList.files)

    if (missingFiles.isEmpty()) {
      logger.info { "No missing files" }
      return
    }

    logger.info { "Missing ${missingFiles.size} files, starting sync" }
    logger.info { "Sync strategy: concurrency=${syncConfig.concurrency}" }

    val sema = Semaphore(syncConfig.concurrency)

    missingFiles.forEach { file ->
      sema.withPermit {
        async {
          try {
            downloadFile(file)
            logger.debug { "Downloaded: ${file.path}" }
          } catch (e: Exception) {
            logger.error(e) { "Failed to download ${file.path}" }
            throw e
          }
        }
      }
    }

    logger.info { "Sync completed: ${missingFiles.size} files" }
  }

  private suspend fun downloadFile(file: FileInfo) {
    val response =
        retry(stopAtAttempts(5)) {
          httpClient.get("${config.clusterBmclapi}${file.path}") {
            header(HttpHeaders.Authorization, "Bearer ${tokenManager.getToken()}")
          }
        }

    val content = response.body<ByteArray>()

    if (!HashUtil.validateFile(content, file.hash)) {
      throw Exception("File validation failed for ${file.path}")
    }

    storage.writeFile(HashUtil.hashToFilename(file.hash), content, file)
  }

  suspend fun connect() {
    // Don't reconnect if already connected
    if (::socket.isInitialized && socket.connected()) {
      logger.debug { "Already connected to server" }
      return
    }

    val opts =
        IO.Options().apply {
          transports = arrayOf("websocket")
          auth = mapOf("token" to tokenManager.getToken())
        }

    socket =
        IO.socket(URI(config.clusterBmclapi), opts).apply {
          on(Socket.EVENT_CONNECT) { logger.debug { "Connected to server" } }

          on(Socket.EVENT_DISCONNECT) { args ->
            val reason = args.firstOrNull()?.toString() ?: "unknown"
            logger.warn { "Disconnected from server: $reason" }
            isEnabled = false
          }

          on("message") { args -> logger.info { "Message: ${args.firstOrNull()}" } }

          on("exception") { args -> logger.error { "Exception: ${args.firstOrNull()}" } }

          on("warden-error") { args -> logger.warn { "Warden error: ${args.firstOrNull()}" } }

          connect()
        }
  }

  suspend fun enable() {
    if (isEnabled) return

    logger.trace { "Enabling cluster" }

    val upnpIp =
        try {
          natService.startIfEnabled()
        } catch (e: Exception) {
          logger.error(e) { "UPnP/NAT 端口映射失败" }
          throw e
        }

    val hostForEnable = config.clusterIp ?: upnpIp?.hostAddress

    val enableRequest =
        EnableRequest(
            host = hostForEnable,
            port = config.clusterPublicPort,
            version = AGENT_PROTOCOL_VERSION,
            byoc = config.byoc,
            flavor =
                mapOf(
                    "runtime" to config.flavor.runtime,
                    "storage" to config.flavor.storage,
                ),
        )

    socket.emitAck("enable", enableRequest)

    isEnabled = true
    wantEnable = true
    logger.info { "Cluster enabled" }
  }

  suspend fun disable() {
    wantEnable = false
    isEnabled = false
    socket.disconnect()
    logger.info { "Cluster disabled" }
  }

  suspend fun requestCert(): CertificateResponse {
    logger.debug { "Requesting certificate from server" }
    return socket.emitAck<CertificateResponse>("request-cert")
  }

  fun close() {
    socket?.close()
  }
}
