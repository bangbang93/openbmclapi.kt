package com.bangbang93.openbmclapi.config

import io.ktor.server.application.*

data class ClusterConfig(
    val clusterId: String,
    val clusterSecret: String,
    val clusterIp: String? = null,
    val port: Int = 4000,
    val clusterPublicPort: Int = port,
    val byoc: Boolean = false,
    val disableAccessLog: Boolean = false,
    val enableNginx: Boolean = false,
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

    return ClusterConfig(
        clusterId =
            env.propertyOrNull("openbmclapi.cluster.id")?.getString()
                ?: System.getenv("CLUSTER_ID")
                ?: System.getProperty("CLUSTER_ID")
                ?: "test-cluster",
        // Default for testing
        clusterSecret =
            env.propertyOrNull("openbmclapi.cluster.secret")?.getString()
                ?: System.getenv("CLUSTER_SECRET")
                ?: System.getProperty("CLUSTER_SECRET")
                ?: "test-secret",
        // Default for testing
        clusterIp =
            env.propertyOrNull("openbmclapi.cluster.ip")?.getString()
                ?: System.getenv("CLUSTER_IP"),
        port =
            env.propertyOrNull("openbmclapi.cluster.port")?.getString()?.toIntOrNull()
                ?: System.getenv("CLUSTER_PORT")?.toIntOrNull()
                ?: 4000,
        clusterPublicPort =
            env.propertyOrNull("openbmclapi.cluster.publicPort")?.getString()?.toIntOrNull()
                ?: System.getenv("CLUSTER_PUBLIC_PORT")?.toIntOrNull()
                ?: env.propertyOrNull("openbmclapi.cluster.port")?.getString()?.toIntOrNull()
                ?: System.getenv("CLUSTER_PORT")?.toIntOrNull()
                ?: 4000,
        byoc =
            env.propertyOrNull("openbmclapi.cluster.byoc")?.getString()?.toBoolean()
                ?: System.getenv("CLUSTER_BYOC")?.toBoolean()
                ?: false,
        disableAccessLog =
            env.propertyOrNull("openbmclapi.cluster.disableAccessLog")?.getString()?.toBoolean()
                ?: System.getenv("DISABLE_ACCESS_LOG")?.toBoolean()
                ?: false,
        enableNginx =
            env.propertyOrNull("openbmclapi.cluster.enableNginx")?.getString()?.toBoolean()
                ?: System.getenv("ENABLE_NGINX")?.toBoolean()
                ?: false,
        enableUpnp =
            env.propertyOrNull("openbmclapi.cluster.enableUpnp")?.getString()?.toBoolean()
                ?: System.getenv("ENABLE_UPNP")?.toBoolean()
                ?: false,
        storage =
            env.propertyOrNull("openbmclapi.storage.type")?.getString()
                ?: System.getenv("CLUSTER_STORAGE")
                ?: "file",
        sslKey =
            env.propertyOrNull("openbmclapi.ssl.key")?.getString()
                ?: System.getenv("SSL_KEY"),
        sslCert =
            env.propertyOrNull("openbmclapi.ssl.cert")?.getString()
                ?: System.getenv("SSL_CERT"),
        clusterBmclapi =
            env.propertyOrNull("openbmclapi.cluster.bmclapi")?.getString()
                ?: System.getenv("CLUSTER_BMCLAPI")
                ?: "https://openbmclapi.bangbang93.com",
    )
}
