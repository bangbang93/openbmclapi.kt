package com.bangbang93.openbmclapi.agent.storage

import com.bangbang93.openbmclapi.agent.storage.config.AlistWebdavStorageConfig
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import io.ktor.utils.io.readRemaining
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.readByteArray
import java.util.concurrent.ConcurrentHashMap

private val logger = KotlinLogging.logger {}

/**
 * Alist WebDAV 存储驱动
 * 支持从 Alist 的 WebDAV 获取 302 重定向地址并缓存
 */
class AlistWebdavStorage(
    configMap: Map<String, String>,
) : WebdavStorage(configMap) {
    private val alistConfig: AlistWebdavStorageConfig = AlistWebdavStorageConfig.fromMap(configMap)
    private val httpClient =
        HttpClient(CIO) {
            followRedirects = false
        }
    private val redirectUrlCache = ConcurrentHashMap<String, CachedRedirectUrl>()
    private val cacheMutex = Mutex()

    data class CachedRedirectUrl(
        val url: String,
        val expiresAt: Long,
    )

    override suspend fun serveFile(
        hashPath: String,
        call: ApplicationCall,
        name: String?,
    ): ServeResult {
        // 检查是否为空文件
        if (emptyFiles.contains(hashPath)) {
            call.respond(HttpStatusCode.OK, ByteArray(0))
            return ServeResult(0, 1)
        }

        // 检查缓存的重定向 URL
        val now = System.currentTimeMillis()
        val cachedUrl = redirectUrlCache[hashPath]
        if (cachedUrl != null && cachedUrl.expiresAt > now) {
            call.respondRedirect(cachedUrl.url, permanent = false)
            val size = files[hashPath.substringAfterLast('/')]?.size ?: 0
            return ServeResult(size, 1)
        }

        // 获取 WebDAV 文件下载链接
        val fullPath = "$baseUrl/$basePath/$hashPath"
        val downloadUrl = client.getFileDownloadLink(fullPath)

        // 发起 HTTP 请求获取实际的文件或重定向
        val response: HttpResponse = httpClient.get(downloadUrl)

        when ( // 2xx 成功响应 - 直接返回内容
            response.status.value
        ) {
            in 200..299 -> {
                val channel = response.bodyAsChannel()
                val content = channel.readRemaining().readByteArray()
                call.respond(HttpStatusCode.OK, content)
                return ServeResult(content.size.toLong(), 1)
            }
            // 3xx 重定向 - 缓存重定向URL并返回给客户端
            in 300..399 -> {
                val location = response.headers["Location"]
                if (location != null) {
                    // 缓存重定向 URL
                    cacheMutex.withLock {
                        redirectUrlCache[hashPath] =
                            CachedRedirectUrl(
                                url = location,
                                expiresAt = now + alistConfig.cacheTtl,
                            )
                    }

                    call.respondRedirect(location, permanent = false)
                    val size = files[hashPath.substringAfterLast('/')]?.size ?: 0
                    return ServeResult(size, 1)
                } else {
                    call.respond(response.status)
                    return ServeResult(0, 0)
                }
            }
            // 其他错误
            else -> {
                call.respond(response.status)
                return ServeResult(0, 0)
            }
        }
    }
}
