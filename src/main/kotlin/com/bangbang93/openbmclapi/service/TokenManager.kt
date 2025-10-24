package com.bangbang93.openbmclapi.service

import com.bangbang93.openbmclapi.config.ClusterConfig
import com.bangbang93.openbmclapi.model.ChallengeResponse
import com.bangbang93.openbmclapi.model.TokenRequest
import com.bangbang93.openbmclapi.model.TokenResponse
import com.bangbang93.openbmclapi.util.HashUtil
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single
import kotlin.time.Duration.Companion.minutes

private val logger = KotlinLogging.logger {}

@Single
class TokenManager(
    config: ClusterConfig,
) {
    private val clusterId = config.clusterId
    private val clusterSecret = config.clusterSecret
    private val version = System.getProperty("app.version") ?: "0.0.1"
    private val prefixUrl = config.clusterBmclapi

    private var token: String? = null

    private val client =
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        prettyPrint = false
                    },
                )
            }
        }

    suspend fun getToken(): String {
        if (token == null) {
            token = fetchToken()
        }
        return token!!
    }

    private suspend fun fetchToken(): String {
        val challengeResponse =
            client.get("$prefixUrl/openbmclapi-agent/challenge") {
                parameter("clusterId", clusterId)
                header(HttpHeaders.UserAgent, "openbmclapi-cluster/$version")
            }.body<ChallengeResponse>()

        val signature = HashUtil.createHmacSha256(clusterSecret, challengeResponse.challenge)

        val tokenResponse =
            client.post("$prefixUrl/openbmclapi-agent/token") {
                contentType(ContentType.Application.Json)
                header(HttpHeaders.UserAgent, "openbmclapi-cluster/$version")
                setBody(
                    TokenRequest(
                        clusterId = clusterId,
                        challenge = challengeResponse.challenge,
                        signature = signature,
                    ),
                )
            }.body<TokenResponse>()

        scheduleRefreshToken(tokenResponse.ttl)
        return tokenResponse.token
    }

    private fun scheduleRefreshToken(ttl: Long) {
        val next = maxOf(ttl - 10.minutes.inWholeMilliseconds, ttl / 2)
        logger.trace { "Scheduling token refresh in ${next}ms" }

        CoroutineScope(Dispatchers.IO).launch {
            delay(next)
            try {
                refreshToken()
            } catch (e: Exception) {
                logger.error(e) { "Failed to refresh token" }
            }
        }
    }

    private suspend fun refreshToken() {
        val tokenResponse =
            client.post("$prefixUrl/openbmclapi-agent/token") {
                contentType(ContentType.Application.Json)
                header(HttpHeaders.UserAgent, "openbmclapi-cluster/$version")
                setBody(
                    TokenRequest(
                        clusterId = clusterId,
                        token = token,
                    ),
                )
            }.body<TokenResponse>()

        logger.debug { "Successfully refreshed token" }
        scheduleRefreshToken(tokenResponse.ttl)
        token = tokenResponse.token
    }

    fun close() {
        client.close()
    }
}
