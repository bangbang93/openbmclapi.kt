package com.bangbang93.openbmclapi.agent

import com.bangbang93.openbmclapi.agent.config.loadConfig
import com.bangbang93.openbmclapi.agent.service.BootstrapService
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopping
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext

private val logger = KotlinLogging.logger {}

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain
        .main(args)
}

fun Application.module() {
    val config = loadConfig()
    val version = environment.config.propertyOrNull("ktor.application.version")?.getString() ?: "0.0.1"

    // Set version for later use
    System.setProperty("app.version", version)

    logger.info { "Starting OpenBMCLAPI Kotlin version $version" }
    logger.info { "Cluster ID: ${config.clusterId}" }

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
                logger.error(e) { "Bootstrap failed" }
            }
        }

        // Register shutdown hook
        monitor.subscribe(ApplicationStopping) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val koin = GlobalContext.get()
                    val bootstrapService = koin.get<BootstrapService>()
                    bootstrapService.shutdown()
                } catch (e: Exception) {
                    logger.error(e) { "Shutdown failed" }
                }
            }
        }
    }
}
