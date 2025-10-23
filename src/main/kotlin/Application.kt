package com.bangbang93.openbmclapi

import com.bangbang93.openbmclapi.config.loadConfig
import com.bangbang93.openbmclapi.model.Counters
import com.bangbang93.openbmclapi.service.BootstrapService
import com.bangbang93.openbmclapi.service.ClusterService
import com.bangbang93.openbmclapi.service.TokenManager
import com.bangbang93.openbmclapi.storage.FileStorage
import com.bangbang93.openbmclapi.storage.IStorage
import io.ktor.server.application.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.dsl.module
import org.slf4j.LoggerFactory
import java.io.File

private val logger = LoggerFactory.getLogger("Application")

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val config = loadConfig()
    val version = environment.config.propertyOrNull("ktor.application.version")?.getString() ?: "0.0.1"
    
    logger.info("Starting OpenBMCLAPI Kotlin version $version")
    logger.info("Cluster ID: ${config.clusterId}")
    
    val appModule = module {
        single { config }
        single { Counters() }
        single<IStorage> { 
            when (config.storage) {
                "file" -> FileStorage(File(System.getProperty("user.dir"), "cache").absolutePath)
                else -> throw IllegalStateException("Unsupported storage type: ${config.storage}")
            }
        }
        single { 
            TokenManager(
                config.clusterId, 
                config.clusterSecret, 
                version,
                config.clusterBmclapi
            ) 
        }
        single {
            ClusterService(
                config,
                get(),
                get(),
                get(),
                version
            )
        }
        single {
            BootstrapService(
                config,
                get(),
                get(),
                get(),
                get(),
                version
            )
        }
        single<HelloService> {
            HelloService {
                println(environment.log.info("Hello, World!"))
            }
        }
    }
    
    configureFrameworks(appModule)
    configureSerialization()
    configureMonitoring()
    configureHTTP()
    configureRouting()
    
    // Initialize cluster in background
    // Note: In a production environment, you might want to wait for this to complete
    // before accepting HTTP requests, or handle errors appropriately
    if (config.clusterId != "test-cluster") {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Get the bootstrap service from Koin after the application starts
                delay(100) // Small delay to ensure Koin is initialized
                val koin = org.koin.core.context.GlobalContext.get()
                val bootstrapService = koin.get<BootstrapService>()
                bootstrapService.bootstrap()
            } catch (e: Exception) {
                logger.error("Bootstrap failed", e)
                // In production, you might want to exit here
            }
        }
        
        // Register shutdown hook
        environment.monitor.subscribe(ApplicationStopping) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val koin = org.koin.core.context.GlobalContext.get()
                    val bootstrapService = koin.get<BootstrapService>()
                    bootstrapService.shutdown()
                } catch (e: Exception) {
                    logger.error("Shutdown failed", e)
                }
            }
        }
    }
}

