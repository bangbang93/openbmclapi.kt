package com.bangbang93.openbmclapi.agent.service

import com.bangbang93.openbmclapi.agent.config.ClusterConfig
import com.bangbang93.openbmclapi.agent.model.Counters
import com.bangbang93.openbmclapi.agent.storage.IStorage
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import kotlin.time.Duration.Companion.minutes

class BootstrapService(
    private val config: ClusterConfig,
    private val storage: IStorage,
    private val tokenManager: TokenManager,
    private val clusterService: ClusterService,
    private val counters: Counters,
    private val version: String,
) {
    private val logger = LoggerFactory.getLogger(BootstrapService::class.java)
    private val keepaliveService = KeepaliveService(1.minutes, clusterService, counters)
    private var checkFileJob: Job? = null

    suspend fun bootstrap() {
        logger.info("Booting OpenBMCLAPI Kotlin $version")

        // Get initial token
        tokenManager.getToken()

        // Connect to cluster
        clusterService.connect()

        // Check storage
        val storageReady = storage.check()
        if (!storageReady) {
            throw Exception("Storage is not ready")
        }

        // Get configuration and file list
        val configuration = clusterService.getConfiguration()
        val files = clusterService.getFileList()
        logger.info("${files.files.size} files available")

        // Sync files
        try {
            clusterService.syncFiles(files, configuration.sync)
        } catch (e: Exception) {
            logger.error("Sync failed", e)
            throw e
        }

        // Garbage collect old files
        logger.info("Starting garbage collection")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = storage.gc(files.files)
                if (result.count == 0) {
                    logger.info("No expired files")
                } else {
                    logger.info("GC complete: deleted ${result.count} files, freed ${result.size} bytes")
                }
            } catch (e: Exception) {
                logger.error("GC error", e)
            }
        }

        // Enable cluster
        try {
            logger.info("Requesting to go online")
            clusterService.enable()
            logger.info("Cluster enabled, serving ${files.files.size} files")

            // Start keepalive
            // keepaliveService.start(clusterService.socket)

            // Schedule periodic file check
            scheduleFileCheck(files.files.maxOfOrNull { it.mtime } ?: 0)
        } catch (e: Exception) {
            logger.error("Failed to enable cluster", e)
            throw e
        }
    }

    private fun scheduleFileCheck(lastModified: Long) {
        checkFileJob?.cancel()
        checkFileJob =
            CoroutineScope(Dispatchers.IO).launch {
                delay(10.minutes)
                try {
                    logger.debug("Refreshing file list")
                    val fileList = clusterService.getFileList(lastModified)
                    if (fileList.files.isEmpty()) {
                        logger.debug("No new files")
                    } else {
                        logger.info("Found ${fileList.files.size} new files")
                        val configuration = clusterService.getConfiguration()
                        clusterService.syncFiles(fileList, configuration.sync)
                    }

                    // Schedule next check
                    val newLastModified = fileList.files.maxOfOrNull { it.mtime } ?: lastModified
                    scheduleFileCheck(newLastModified)
                } catch (e: Exception) {
                    logger.error("File check error", e)
                    scheduleFileCheck(lastModified) // Retry
                }
            }
    }

    suspend fun shutdown() {
        logger.info("Shutting down")
        checkFileJob?.cancel()
        keepaliveService.stop()
        clusterService.disable()
        clusterService.close()
        tokenManager.close()
    }
}
