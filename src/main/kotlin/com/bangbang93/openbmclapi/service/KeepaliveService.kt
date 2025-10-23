package com.bangbang93.openbmclapi.service

import com.bangbang93.openbmclapi.model.Counters
import com.bangbang93.openbmclapi.model.KeepAliveRequest
import io.socket.client.Socket
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import java.time.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

class KeepaliveService(
    private val interval: Duration = 1.minutes,
    private val clusterService: ClusterService,
    private val counters: Counters
) {
    private val logger = LoggerFactory.getLogger(KeepaliveService::class.java)
    private var job: Job? = null
    private var keepAliveError = 0
    private var socket: Socket? = null

    fun start(socket: Socket) {
        this.socket = socket
        schedule()
    }

    fun stop() {
        job?.cancel()
        job = null
    }

    private fun schedule() {
        job?.cancel()
        job = CoroutineScope(Dispatchers.IO).launch {
            delay(interval)
            logger.trace("Starting keep alive")
            try {
                emitKeepAlive()
            } catch (e: Exception) {
                logger.error("Keep alive failed", e)
            }
        }
    }

    private suspend fun emitKeepAlive() {
        try {
            withTimeout(10000) { // 10 seconds timeout
                val status = keepAlive()
                if (!status) {
                    logger.error("Kicked by server")
                    restart()
                }
                keepAliveError = 0
            }
        } catch (e: TimeoutCancellationException) {
            keepAliveError++
            logger.error("Keep alive timeout")
            if (keepAliveError >= 3) {
                restart()
            }
        } catch (e: Exception) {
            keepAliveError++
            logger.error("Keep alive error", e)
            if (keepAliveError >= 3) {
                restart()
            }
        } finally {
            schedule()
        }
    }

    private suspend fun keepAlive(): Boolean {
        if (!clusterService.isEnabled) {
            throw Exception("Node is not enabled")
        }
        if (socket == null) {
            throw Exception("Not connected to server")
        }

        val currentCounters = Counters(counters.hits, counters.bytes)
        
        val request = KeepAliveRequest(
            time = Instant.now().toString(),
            hits = currentCounters.hits,
            bytes = currentCounters.bytes
        )

        // In a real implementation, this would emit to the socket and wait for acknowledgment
        // For now, we'll simulate success
        logger.info("Keep alive success, served ${currentCounters.hits} files, ${currentCounters.bytes} bytes")
        
        // Reset counters
        counters.hits -= currentCounters.hits
        counters.bytes -= currentCounters.bytes
        
        return true
    }

    private suspend fun restart() {
        try {
            withTimeout(10.minutes) {
                clusterService.disable()
                delay(1000)
                clusterService.connect()
                clusterService.enable()
            }
        } catch (e: Exception) {
            logger.error("Restart failed", e)
            // In production, this would exit the application
        }
    }
}
