package com.bangbang93.openbmclapi.agent.model

import kotlinx.serialization.Serializable
import org.koin.core.annotation.Single
import kotlin.concurrent.atomics.AtomicLong
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@Serializable
data class FileInfo(
    val path: String,
    val hash: String,
    val size: Long,
    val mtime: Long,
)

@Serializable
data class FileList(
    val files: List<FileInfo>,
)

@Serializable
data class SyncConfig(
    val source: String,
    val concurrency: Int,
)

@Serializable
data class OpenbmclapiAgentConfiguration(
    val sync: SyncConfig,
)

@Serializable
data class ChallengeResponse(
    val challenge: String,
)

@Serializable
data class TokenRequest(
    val clusterId: String,
    val challenge: String? = null,
    val signature: String? = null,
    val token: String? = null,
)

@Serializable
data class TokenResponse(
    val token: String,
    val ttl: Long,
)

@Serializable
data class KeepAliveRequest(
    val time: String,
    val hits: Long,
    val bytes: Long,
)

@Serializable
data class EnableRequest(
    val host: String?,
    val port: Int,
    val version: String,
    val byoc: Boolean,
    val noFastEnable: Boolean = false,
    val flavor: Map<String, String>,
)

@Serializable
data class CertificateResponse(
    val cert: String,
    val key: String,
)

@OptIn(ExperimentalAtomicApi::class)
@Single
class Counters {
    val hits = AtomicLong(0L)
    val bytes = AtomicLong(0L)
}

data class GCCounter(
    val count: Int,
    val size: Long,
)
