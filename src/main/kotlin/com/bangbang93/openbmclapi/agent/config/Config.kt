package com.bangbang93.openbmclapi.agent.config

import io.github.cdimascio.dotenv.Dotenv
import io.ktor.server.application.Application

data class ClusterConfig(
    val clusterId: String,
    val clusterSecret: String,
    val clusterIp: String? = null,
    val port: Int = 4000,
    val clusterPublicPort: Int = port,
    val byoc: Boolean = false,
    val disableAccessLog: Boolean = false,
    val enableUpnp: Boolean = false,
    val storage: String = "file",
    val storageOpts: Map<String, String> = emptyMap(),
    val sslKey: String? = null,
    val sslCert: String? = null,
    val clusterBmclapi: String = "https://openbmclapi.bangbang93.com",
) {
    val flavor: ConfigFlavor =
        ConfigFlavor(
            runtime = "Kotlin/${KotlinVersion.CURRENT}",
            storage = storage,
        )
}

data class ConfigFlavor(
    val runtime: String,
    val storage: String,
)

fun Application.loadConfig(): ClusterConfig {
    val env = environment.config
    // 使用 dotenv-kotlin 加载 .env（若不存在则忽略），优先级低于 Ktor config，但高于系统默认值
    val dotenv: Dotenv = Dotenv.configure().ignoreIfMissing().load()

    // Helper: 返回第一个非空且非空白的值（按提供顺序）
    fun firstNonBlank(vararg suppliers: () -> String?): String? {
        for (s in suppliers) {
            val v = s()?.takeIf { it.isNotBlank() }
            if (v != null) return v
        }
        return null
    }

    // Helper: 尝试按顺序查找字符串值（ktor config, dotenv, env, system prop）
    fun lookupString(
        ktorKey: String?,
        dotenvKey: String?,
    ): String? =
        firstNonBlank(
            { ktorKey?.let { env.propertyOrNull(it)?.getString() } },
            { dotenvKey?.let { dotenv[it] } },
            { System.getenv(dotenvKey ?: "")?.takeIf { it.isNotBlank() } },
            { System.getProperty(dotenvKey ?: "")?.takeIf { it.isNotBlank() } },
        )

    return ClusterConfig(
        clusterId =
            lookupString("openbmclapi.cluster.id", "CLUSTER_ID") ?: "test-cluster",
        // Default for testing
        clusterSecret =
            lookupString("openbmclapi.cluster.secret", "CLUSTER_SECRET") ?: "test-secret",
        // Default for testing
        clusterIp =
            lookupString("openbmclapi.cluster.ip", "CLUSTER_IP"),
        port =
            lookupString("openbmclapi.cluster.port", "CLUSTER_PORT")?.toIntOrNull() ?: 4000,
        clusterPublicPort =
            lookupString("openbmclapi.cluster.publicPort", "CLUSTER_PUBLIC_PORT")?.toIntOrNull()
                ?: lookupString("openbmclapi.cluster.port", "CLUSTER_PORT")?.toIntOrNull()
                ?: 4000,
        byoc =
            lookupString("openbmclapi.cluster.byoc", "CLUSTER_BYOC")?.toBoolean() ?: false,
        disableAccessLog =
            lookupString("openbmclapi.cluster.disableAccessLog", "DISABLE_ACCESS_LOG")?.toBoolean() ?: false,
        enableUpnp =
            lookupString("openbmclapi.cluster.enableUpnp", "ENABLE_UPNP")?.toBoolean() ?: false,
        storage =
            lookupString("openbmclapi.storage.type", "CLUSTER_STORAGE") ?: "file",
        sslKey =
            lookupString("openbmclapi.ssl.key", "SSL_KEY"),
        sslCert =
            lookupString("openbmclapi.ssl.cert", "SSL_CERT"),
        clusterBmclapi =
            lookupString("openbmclapi.cluster.bmclapi", "CLUSTER_BMCLAPI") ?: "https://openbmclapi.bangbang93.com",
    )
}
