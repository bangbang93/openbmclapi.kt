package com.bangbang93.openbmclapi.agent

import com.bangbang93.openbmclapi.agent.config.ClusterConfig
import com.bangbang93.openbmclapi.agent.config.Version
import com.bangbang93.openbmclapi.agent.service.BootstrapService
import com.bangbang93.openbmclapi.agent.service.CertificateService
import com.bangbang93.openbmclapi.agent.service.ClusterService
import com.bangbang93.openbmclapi.agent.service.TokenManager
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationEnvironment
import io.ktor.server.application.ApplicationStopPreparing
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.engine.sslConnector
import io.ktor.server.netty.Netty
import kotlinx.coroutines.runBlocking
import org.koin.core.context.startKoin
import org.koin.ksp.generated.module
import org.koin.ktor.ext.inject
import org.koin.logger.slf4jLogger
import java.io.File
import java.io.FileInputStream
import java.security.KeyStore

private val logger = KotlinLogging.logger {}

suspend fun main() {
    // Start Koin for dependency injection
    val koinApp =
        startKoin {
            slf4jLogger()
            modules(
                AppModule().module,
            )
        }

    val config = koinApp.koin.get<ClusterConfig>()

    logger.info { "Starting OpenBMCLAPI Kotlin version ${Version.current}" }
    logger.info { "Cluster ID: ${config.clusterId}" }

    // Determine if we should use HTTPS and setup certificates
    var useHttps = false
    var keystorePath: String? = null

    try {
        val tokenManager = koinApp.koin.get<TokenManager>()
        val clusterService = koinApp.koin.get<ClusterService>()
        val certificateService = koinApp.koin.get<CertificateService>()

        // Get token first
        tokenManager.getToken()

        // Connect to cluster (needed for non-BYOC certificate requests)
        clusterService.connect()

        // Setup certificates (will either load local or request from server)
        useHttps = certificateService.setupCertificates()

        if (useHttps) {
            keystorePath = certificateService.getKeystorePath()
            logger.info { "HTTPS enabled with keystore at: $keystorePath" }
        } else {
            logger.info { "HTTP mode - no certificates available" }
        }
    } catch (e: Exception) {
        logger.warn(e) { "Failed to setup certificates, using HTTP" }
        useHttps = false
    }

    val env = koinApp.koin.get<ApplicationEnvironment>()

    // Create and start embedded server with appropriate configuration
    val server =
        embeddedServer(Netty, env, configure = {
            // Add HTTPS connector if certificates are available, otherwise HTTP
            if (useHttps && keystorePath != null && File(keystorePath).exists()) {
                try {
                    val keyStore = KeyStore.getInstance("JKS")
                    FileInputStream(keystorePath).use { fis ->
                        keyStore.load(fis, "openbmclapi".toCharArray())
                    }

                    sslConnector(
                        keyStore = keyStore,
                        keyAlias = "openbmclapi",
                        keyStorePassword = { "openbmclapi".toCharArray() },
                        privateKeyPassword = { "openbmclapi".toCharArray() },
                    ) {
                        port = config.port
                        host = "0.0.0.0"
                    }
                    logger.info { "HTTPS connector configured on port ${config.port}" }
                } catch (e: Exception) {
                    logger.error(e) { "Failed to configure HTTPS connector, falling back to HTTP" }
                    connector {
                        port = config.port
                        host = "0.0.0.0"
                    }
                }
            } else {
                // HTTP connector
                connector {
                    port = config.port
                    host = "0.0.0.0"
                }
            }
        }, Application::module)

    server.start(wait = true)
}

suspend fun Application.module() {
    configureSerialization()
    configureMonitoring()
    configureHTTP()
    configureRouting()

    val bootstrapService by inject<BootstrapService>()

    bootstrapService.bootstrap()

    // Register shutdown hook
    monitor.subscribe(ApplicationStopPreparing) {
        runBlocking {
            try {
                bootstrapService.shutdown()
            } catch (e: Exception) {
                logger.error(e) { "Shutdown failed" }
            }
        }
    }
}
