package com.bangbang93.openbmclapi.agent

import com.bangbang93.openbmclapi.agent.config.Version
import com.bangbang93.openbmclapi.agent.config.loadConfig
import com.bangbang93.openbmclapi.agent.service.BootstrapService
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopping
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.core.context.GlobalContext

private val logger = KotlinLogging.logger {}

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain
        .main(args)
}

fun Application.module() {
    val config = loadConfig()

    logger.info { "Starting OpenBMCLAPI Kotlin version ${Version.current}" }
    logger.info { "Cluster ID: ${config.clusterId}" }

    configureFrameworks(config)
    configureSerialization()
    configureMonitoring()
    configureHTTP()
    configureRouting()

    val koin = GlobalContext.get()
    val bootstrapService = koin.get<BootstrapService>()

    // Initialize cluster in background
    if (config.clusterId != "test-cluster") {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                bootstrapService.bootstrap()
            } catch (e: Exception) {
                logger.error(e) { "Bootstrap failed" }
            }
        }

        // Register shutdown hook
        monitor.subscribe(ApplicationStopping) {
            runBlocking {
                try {
                    bootstrapService.shutdown()
                } catch (e: Exception) {
                    logger.error(e) { "Shutdown failed" }
                }
            }
        }
    }
}
