package com.bangbang93.openbmclapi.agent.service

import com.bangbang93.openbmclapi.agent.model.Counters
import com.bangbang93.openbmclapi.agent.model.KeepAliveRequest
import com.bangbang93.openbmclapi.agent.util.emitAck
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
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.minusAssign
import kotlin.concurrent.atomics.plusAssign
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
    private lateinit var socket: Socket

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
            withTimeout(10000) {
                // 10 seconds timeout
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

    @OptIn(ExperimentalAtomicApi::class)
    private suspend fun keepAlive(): Boolean {
        if (!clusterService.isEnabled) {
            throw Exception("Node is not enabled")
        }

        val currentHits = counters.hits.load()
        val currentBytes = counters.bytes.load()

        val request =
            KeepAliveRequest(
                time = Instant.now().toString(),
                hits = currentHits,
                bytes = currentBytes,
            )

        socket.emitAck("keep-alive", request)
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
