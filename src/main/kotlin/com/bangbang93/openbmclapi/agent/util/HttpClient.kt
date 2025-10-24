package com.bangbang93.openbmclapi.agent.util

import com.bangbang93.openbmclapi.agent.config.AGENT_PROTOCOL_VERSION
import com.bangbang93.openbmclapi.agent.config.Version
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single

@Single
fun httpClient() =
    HttpClient(CIO) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    prettyPrint = false
                },
            )
        }
        install(UserAgent) {
            agent = "openbmclapi-cluster/$AGENT_PROTOCOL_VERSION openbmclapi.kt/${Version.current}"
        }
    }
