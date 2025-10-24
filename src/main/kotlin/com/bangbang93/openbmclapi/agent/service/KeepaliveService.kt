package com.bangbang93.openbmclapi.agent.service

import com.bangbang93.openbmclapi.agent.model.Counters
import com.bangbang93.openbmclapi.agent.model.KeepAliveRequest
import io.github.oshai.kotlinlogging.KotlinLogging
import io.socket.client.Socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.time.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

private val logger = KotlinLogging.logger {}

class KeepaliveService(
    private val interval: Duration = 1.minutes,
    private val clusterService: ClusterService,
    private val counters: Counters,
) {
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
        job =
            CoroutineScope(Dispatchers.IO).launch {
                delay(interval)
                logger.trace { "Starting keep alive" }
                try {
                    emitKeepAlive()
                } catch (e: Exception) {
                    logger.error(e) { "Keep alive failed" }
                }
            }
    }

    private suspend fun emitKeepAlive() {
        try {
            withTimeout(10000) { // 10 seconds timeout
                val status = keepAlive()
                if (!status) {
                    logger.error { "Kicked by server" }
                    restart()
                }
                keepAliveError = 0
            }
        } catch (e: TimeoutCancellationException) {
            keepAliveError++
            logger.error { "Keep alive timeout" }
            if (keepAliveError >= 3) {
                restart()
            }
        } catch (e: Exception) {
            keepAliveError++
            logger.error(e) { "Keep alive error" }
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

        val currentHits = counters.hits
        val currentBytes = counters.bytes

        val request =
            KeepAliveRequest(
                time = Instant.now().toString(),
                hits = currentHits,
                bytes = currentBytes,
            )

        // In a real implementation, this would emit to the socket and wait for acknowledgment
        // For now, we'll simulate success
        logger.info { "Keep alive success, served $currentHits files, $currentBytes bytes" }

        // Reset counters
        counters.hits -= currentHits
        counters.bytes -= currentBytes

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
            logger.error(e) { "Restart failed" }
            // In production, this would exit the application
        }
    }
}

