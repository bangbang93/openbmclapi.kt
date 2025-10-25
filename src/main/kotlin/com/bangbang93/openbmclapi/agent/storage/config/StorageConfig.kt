package com.bangbang93.openbmclapi.agent.storage.config

/**
 * WebDAV 存储配置
 */
data class WebdavStorageConfig(
    val url: String,
    val username: String? = null,
    val password: String? = null,
    val basePath: String,
) {
    init {
        require(url.isNotBlank()) { "WebDAV url 不能为空" }
        require(basePath.isNotBlank()) { "WebDAV basePath 不能为空" }
    }

    companion object {
        fun fromMap(config: Map<String, String>): WebdavStorageConfig {
            val url = config["url"] ?: throw IllegalArgumentException("WebDAV url 是必需的")
            val basePath = config["basePath"] ?: throw IllegalArgumentException("WebDAV basePath 是必需的")
            return WebdavStorageConfig(
                url = url,
                username = config["username"],
                password = config["password"],
                basePath = basePath,
            )
        }
    }
}

/**
 * Alist WebDAV 存储配置
 */
data class AlistWebdavStorageConfig(
    val url: String,
    val username: String? = null,
    val password: String? = null,
    val basePath: String,
    // 默认 1 小时，单位毫秒
    val cacheTtl: Long = 3600000,
) {
    init {
        require(url.isNotBlank()) { "Alist WebDAV url 不能为空" }
        require(basePath.isNotBlank()) { "Alist WebDAV basePath 不能为空" }
        require(cacheTtl > 0) { "cacheTtl 必须大于 0" }
    }

    companion object {
        fun fromMap(config: Map<String, String>): AlistWebdavStorageConfig {
            val url = config["url"] ?: throw IllegalArgumentException("Alist WebDAV url 是必需的")
            val basePath = config["basePath"] ?: throw IllegalArgumentException("Alist WebDAV basePath 是必需的")
            val cacheTtl = config["cacheTtl"]?.toLongOrNull() ?: 3600000L

            return AlistWebdavStorageConfig(
                url = url,
                username = config["username"],
                password = config["password"],
                basePath = basePath,
                cacheTtl = cacheTtl,
            )
        }
    }
}

/**
 * MinIO 存储配置
 */
data class MinioStorageConfig(
    val url: String,
    val internalUrl: String? = null,
) {
    init {
        require(url.isNotBlank()) { "MinIO url 不能为空" }
        // 验证 URL 格式并确保包含 bucket
        try {
            val uri = java.net.URI.create(url)
            val pathParts = uri.path.split("/").filter { it.isNotEmpty() }
            require(pathParts.isNotEmpty()) {
                "MinIO url 必须包含 bucket 路径，格式: scheme://key:secret@host:port/bucket[/prefix]"
            }
        } catch (e: Exception) {
            throw IllegalArgumentException("MinIO url 格式无效: ${e.message}")
        }
    }

    companion object {
        fun fromMap(config: Map<String, String>): MinioStorageConfig {
            val url = config["url"] ?: throw IllegalArgumentException("MinIO url 是必需的")
            return MinioStorageConfig(
                url = url,
                internalUrl = config["internalUrl"],
            )
        }
    }
}

/**
 * OSS 存储配置
 */
data class OssStorageConfig(
    val accessKeyId: String,
    val accessKeySecret: String,
    val bucket: String,
    val endpoint: String = "oss-cn-hangzhou.aliyuncs.com",
    val prefix: String = "",
    val proxy: Boolean = true,
) {
    init {
        require(accessKeyId.isNotBlank()) { "OSS accessKeyId 不能为空" }
        require(accessKeySecret.isNotBlank()) { "OSS accessKeySecret 不能为空" }
        require(bucket.isNotBlank()) { "OSS bucket 不能为空" }
        require(endpoint.isNotBlank()) { "OSS endpoint 不能为空" }
    }

    companion object {
        fun fromMap(config: Map<String, String>): OssStorageConfig {
            val accessKeyId = config["accessKeyId"] ?: throw IllegalArgumentException("OSS accessKeyId 是必需的")
            val accessKeySecret = config["accessKeySecret"] ?: throw IllegalArgumentException("OSS accessKeySecret 是必需的")
            val bucket = config["bucket"] ?: throw IllegalArgumentException("OSS bucket 是必需的")

            return OssStorageConfig(
                accessKeyId = accessKeyId,
                accessKeySecret = accessKeySecret,
                bucket = bucket,
                endpoint = config["endpoint"] ?: "oss-cn-hangzhou.aliyuncs.com",
                prefix = config["prefix"] ?: "",
                proxy = config["proxy"]?.toBoolean() ?: true,
            )
        }
    }
}
