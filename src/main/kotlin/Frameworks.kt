package com.bangbang93.openbmclapi

import com.bangbang93.openbmclapi.config.ClusterConfig
import io.ktor.server.application.*
import org.koin.dsl.module
import org.koin.ksp.generated.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureFrameworks(config: ClusterConfig) {
    install(Koin) {
        slf4jLogger()
        modules(
            module {
                single { config }
            },
            AppModule().module,
        )
    }
}
