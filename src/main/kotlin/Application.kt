package com.bangbang93.openbmclapi

import com.bangbang93.openbmclapi.config.loadConfig
import com.bangbang93.openbmclapi.model.Counters
import com.bangbang93.openbmclapi.service.TokenManager
import com.bangbang93.openbmclapi.storage.FileStorage
import com.bangbang93.openbmclapi.storage.IStorage
import io.ktor.server.application.*
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
}

