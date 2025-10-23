package com.bangbang93.openbmclapi.service

import com.bangbang93.openbmclapi.model.ChallengeResponse
import com.bangbang93.openbmclapi.model.TokenRequest
import com.bangbang93.openbmclapi.model.TokenResponse
import com.bangbang93.openbmclapi.util.HashUtil
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import kotlin.time.Duration.Companion.minutes

class TokenManager(
    private val clusterId: String,
    private val clusterSecret: String,
    private val version: String,
    private val prefixUrl: String
) {
    private val logger = LoggerFactory.getLogger(TokenManager::class.java)
    private var token: String? = null

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = false
            })
        }
    }

    suspend fun getToken(): String {
        if (token == null) {
            token = fetchToken()
        }
        return token!!
    }

    private suspend fun fetchToken(): String {
        val challengeResponse = client.get("$prefixUrl/openbmclapi-agent/challenge") {
            parameter("clusterId", clusterId)
            header(HttpHeaders.UserAgent, "openbmclapi-cluster/$version")
        }.body<ChallengeResponse>()

        val signature = HashUtil.createHmacSha256(clusterSecret, challengeResponse.challenge)

        val tokenResponse = client.post("$prefixUrl/openbmclapi-agent/token") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.UserAgent, "openbmclapi-cluster/$version")
            setBody(TokenRequest(
                clusterId = clusterId,
                challenge = challengeResponse.challenge,
                signature = signature
            ))
        }.body<TokenResponse>()

        scheduleRefreshToken(tokenResponse.ttl)
        return tokenResponse.token
    }

    private fun scheduleRefreshToken(ttl: Long) {
        val next = maxOf(ttl - 10.minutes.inWholeMilliseconds, ttl / 2)
        logger.trace("Scheduling token refresh in ${next}ms")
        
        CoroutineScope(Dispatchers.IO).launch {
            delay(next)
            try {
                refreshToken()
            } catch (e: Exception) {
                logger.error("Failed to refresh token", e)
            }
        }
    }

    private suspend fun refreshToken() {
        val tokenResponse = client.post("$prefixUrl/openbmclapi-agent/token") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.UserAgent, "openbmclapi-cluster/$version")
            setBody(TokenRequest(
                clusterId = clusterId,
                token = token
            ))
        }.body<TokenResponse>()

        logger.debug("Successfully refreshed token")
        scheduleRefreshToken(tokenResponse.ttl)
        token = tokenResponse.token
    }

    fun close() {
        client.close()
    }
}
