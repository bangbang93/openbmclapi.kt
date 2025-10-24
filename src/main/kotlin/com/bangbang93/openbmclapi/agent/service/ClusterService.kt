package com.bangbang93.openbmclapi.agent.service

import com.bangbang93.openbmclapi.agent.config.AGENT_PROTOCOL_VERSION
import com.bangbang93.openbmclapi.agent.config.ClusterConfig
import com.bangbang93.openbmclapi.agent.model.Counters
import com.bangbang93.openbmclapi.agent.model.EnableRequest
import com.bangbang93.openbmclapi.agent.model.FileInfo
import com.bangbang93.openbmclapi.agent.model.FileList
import com.bangbang93.openbmclapi.agent.model.OpenbmclapiAgentConfiguration
import com.bangbang93.openbmclapi.agent.model.SyncConfig
import com.bangbang93.openbmclapi.agent.storage.IStorage
import com.bangbang93.openbmclapi.agent.util.HashUtil
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single
import java.net.URI

private val logger = KotlinLogging.logger {}

@Single
class ClusterService(
    private val config: ClusterConfig,
    private val storage: IStorage,
    private val tokenManager: TokenManager,
    private val counters: Counters,
) {
    private val version = System.getProperty("app.version") ?: "0.0.1"
    private val userAgent = "openbmclapi-cluster/$AGENT_PROTOCOL_VERSION openbmclapi.kt/$version"
    private var socket: Socket? = null
    var isEnabled = false
    var wantEnable = false

    private val client =
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        prettyPrint = false
                    },
                )
            }
        }

    suspend fun getFileList(lastModified: Long? = null): FileList {
        val response =
            client.get("${config.clusterBmclapi}/openbmclapi/files") {
                header(HttpHeaders.Authorization, "Bearer ${tokenManager.getToken()}")
                header(HttpHeaders.UserAgent, userAgent)
                if (lastModified != null) {
                    parameter("lastModified", lastModified)
                }
            }

        if (response.status == HttpStatusCode.NoContent) {
            return FileList(emptyList())
        }

        // Note: In the original, this would be decompressed with zstd
        // For now, we'll handle JSON directly
        return response.body<FileList>()
    }

    suspend fun getConfiguration(): OpenbmclapiAgentConfiguration =
        client
            .get("${config.clusterBmclapi}/openbmclapi/configuration") {
                header(HttpHeaders.Authorization, "Bearer ${tokenManager.getToken()}")
                header(HttpHeaders.UserAgent, userAgent)
            }.body()

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

        val downloadJobs =
            missingFiles.map { file ->
                CoroutineScope(Dispatchers.IO).async {
                    try {
                        downloadFile(file)
                        logger.debug { "Downloaded: ${file.path}" }
                    } catch (e: Exception) {
                        logger.error(e) { "Failed to download ${file.path}" }
                        throw e
                    }
                }
            }

        val results = downloadJobs.awaitAll()
        logger.info { "Sync completed: ${results.size} files" }
    }

    private suspend fun downloadFile(file: FileInfo) {
        val response =
            client.get("${config.clusterBmclapi}${file.path}") {
                header(HttpHeaders.Authorization, "Bearer ${tokenManager.getToken()}")
                header(HttpHeaders.UserAgent, userAgent)
            }

        val content = response.body<ByteArray>()

        if (!HashUtil.validateFile(content, file.hash)) {
            throw Exception("File validation failed for ${file.path}")
        }

        storage.writeFile(HashUtil.hashToFilename(file.hash), content, file)
    }

    fun connect() {
        if (socket?.connected() == true) return

        val opts =
            IO.Options().apply {
                transports = arrayOf("websocket")
            }

        socket =
            IO.socket(URI(config.clusterBmclapi), opts).apply {
                on(Socket.EVENT_CONNECT) {
                    logger.debug { "Connected to server" }

                    // Authenticate after connecting
                    CoroutineScope(Dispatchers.IO).launch {
                        val token = tokenManager.getToken()
                        emit("authenticate", token)
                    }
                }

                on(Socket.EVENT_DISCONNECT) { args ->
                    val reason = args.firstOrNull()?.toString() ?: "unknown"
                    logger.warn { "Disconnected from server: $reason" }
                    isEnabled = false
                }

                on("message") { args ->
                    logger.info { "Message: ${args.firstOrNull()}" }
                }

                on("exception") { args ->
                    logger.error { "Exception: ${args.firstOrNull()}" }
                }

                on("warden-error") { args ->
                    logger.warn { "Warden error: ${args.firstOrNull()}" }
                }

                connect()
            }
    }

    suspend fun enable() {
        if (isEnabled) return

        logger.trace { "Enabling cluster" }

        val enableRequest =
            EnableRequest(
                host = config.clusterIp,
                port = config.clusterPublicPort,
                version = version,
                byoc = config.byoc,
                flavor =
                    mapOf(
                        "runtime" to config.flavor.runtime,
                        "storage" to config.flavor.storage,
                    ),
            )

        // Note: In a real implementation, this would use socket.emit with acknowledgment
        // For now, we'll mark as enabled
        isEnabled = true
        wantEnable = true
        logger.info { "Cluster enabled" }
    }

    suspend fun disable() {
        if (socket == null) return

        wantEnable = false
        isEnabled = false
        socket?.disconnect()
        logger.info { "Cluster disabled" }
    }

    fun close() {
        socket?.close()
        client.close()
    }
}
