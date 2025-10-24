package com.bangbang93.openbmclapi

import com.bangbang93.openbmclapi.config.loadConfig
import com.bangbang93.openbmclapi.service.BootstrapService
import io.ktor.server.application.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("Application")

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val config = loadConfig()
    val version = environment.config.propertyOrNull("ktor.application.version")?.getString() ?: "0.0.1"

    // Set version for later use
    System.setProperty("app.version", version)

    logger.info("Starting OpenBMCLAPI Kotlin version $version")
    logger.info("Cluster ID: ${config.clusterId}")

    configureFrameworks(config)
    configureSerialization()
    configureMonitoring()
    configureHTTP()
    configureRouting()

    // Initialize cluster in background
    if (config.clusterId != "test-cluster") {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                delay(100)
                val koin = GlobalContext.get()
                val bootstrapService = koin.get<BootstrapService>()
                bootstrapService.bootstrap()
            } catch (e: Exception) {
                logger.error("Bootstrap failed", e)
            }
        }

        // Register shutdown hook
        environment.monitor.subscribe(ApplicationStopping) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val koin = GlobalContext.get()
                    val bootstrapService = koin.get<BootstrapService>()
                    bootstrapService.shutdown()
                } catch (e: Exception) {
                    logger.error("Shutdown failed", e)
                }
            }
        }
    }
}
