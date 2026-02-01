package com.bangbang93.openbmclapi.agent.storage

import com.bangbang93.openbmclapi.agent.model.FileInfo
import com.bangbang93.openbmclapi.agent.model.GCCounter
import com.bangbang93.openbmclapi.agent.storage.config.WebdavStorageConfig
import com.github.sardine.Sardine
import com.github.sardine.SardineFactory
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import java.io.ByteArrayInputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val logger = KotlinLogging.logger {}

open class WebdavStorage(configMap: Map<String, String>) : IStorage {
    protected val config: WebdavStorageConfig = WebdavStorageConfig.fromMap(configMap)
    protected val client: Sardine
    protected val baseUrl: String
    protected val basePath: String
    protected val files = mutableMapOf<String, FileMetadata>()
    protected val emptyFiles = mutableSetOf<String>()

    data class FileMetadata(val size: Long, val path: String)

    init {
        client =
            if (config.username != null && config.password != null) {
                SardineFactory.begin(config.username, config.password)
            } else {
                SardineFactory.begin()
            }

        baseUrl = config.url.trimEnd('/')
        basePath = config.basePath.trim('/')
    }

    override suspend fun init() {
        withContext(Dispatchers.IO) {
            val fullPath = "$baseUrl/$basePath"
            if (!client.exists(fullPath)) {
                logger.info { "Creating base path: $basePath" }
                client.createDirectory(fullPath)
            }
        }
    }

    override suspend fun check(): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val checkPath = "$baseUrl/$basePath/.check"
                val content = System.currentTimeMillis().toString().toByteArray()
                client.put(checkPath, ByteArrayInputStream(content), "application/octet-stream")
                true
            } catch (e: Exception) {
                logger.error(e) { "Storage check failed" }
                false
            } finally {
                try {
                    client.delete("$baseUrl/$basePath/.check")
                } catch (e: Exception) {
                    logger.warn(e) { "Failed to delete check file" }
                }
            }
        }

    override suspend fun writeFile(path: String, content: ByteArray, fileInfo: FileInfo) =
        withContext(Dispatchers.IO) {
            if (content.isEmpty()) {
                emptyFiles.add(path)
                return@withContext
            }

            val fullPath = "$baseUrl/$basePath/$path"
            val parentPath = fullPath.substringBeforeLast('/')

            if (!client.exists(parentPath)) {
                client.createDirectory(parentPath)
            }

            client.put(fullPath, ByteArrayInputStream(content), "application/octet-stream")
            files[fileInfo.hash] = FileMetadata(content.size.toLong(), fileInfo.path)
        }

    override suspend fun exists(path: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                client.exists("$baseUrl/$basePath/$path")
            } catch (e: Exception) {
                false
            }
        }

    override suspend fun getMissingFiles(files: List<FileInfo>): List<FileInfo> =
        withContext(Dispatchers.IO) {
            val remoteFileMap = files.associateBy { it.hash }.toMutableMap()

            if (this@WebdavStorage.files.isNotEmpty()) {
                for (hash in this@WebdavStorage.files.keys) {
                    remoteFileMap.remove(hash)
                }
                return@withContext remoteFileMap.values.toList()
            }

            val queue = mutableListOf("$baseUrl/$basePath")
            while (queue.isNotEmpty()) {
                val dir = queue.removeAt(0)
                try {
                    val resources = client.list(dir)
                    for (resource in resources) {
                        if (resource.isDirectory) {
                            if (!resource.href.toString().endsWith("/$basePath/")) {
                                queue.add(resource.href.toString().trimEnd('/'))
                            }
                        } else {
                            val hash = resource.name
                            val file = remoteFileMap[hash]
                            if (file != null && file.size == resource.contentLength) {
                                this@WebdavStorage.files[hash] =
                                    FileMetadata(resource.contentLength, resource.path)
                                remoteFileMap.remove(hash)
                            }
                        }
                    }
                } catch (e: Exception) {
                    logger.error(e) { "Failed to list directory: $dir" }
                }
            }

            remoteFileMap.values.toList()
        }

    override suspend fun gc(files: List<FileInfo>): GCCounter =
        withContext(Dispatchers.IO) {
            val fileSet = files.map { it.hash }.toSet()
            var count = 0
            var size = 0L

            val queue = mutableListOf("$baseUrl/$basePath")
            while (queue.isNotEmpty()) {
                val dir = queue.removeAt(0)
                try {
                    val resources = client.list(dir)
                    for (resource in resources) {
                        if (resource.isDirectory) {
                            if (!resource.href.toString().endsWith("/$basePath/")) {
                                queue.add(resource.href.toString().trimEnd('/'))
                            }
                        } else {
                            val hash = resource.name
                            if (!fileSet.contains(hash)) {
                                logger.info { "Deleting expired file: ${resource.path}" }
                                client.delete(resource.href.toString())
                                this@WebdavStorage.files.remove(hash)
                                count++
                                size += resource.contentLength
                            }
                        }
                    }
                } catch (e: Exception) {
                    logger.error(e) { "Failed to GC directory: $dir" }
                }
            }

            GCCounter(count, size)
        }

    override suspend fun serveFile(
        hashPath: String,
        call: ApplicationCall,
        name: String?,
    ): ServeResult {
        if (emptyFiles.contains(hashPath)) {
            call.respond(HttpStatusCode.OK, ByteArray(0))
            return ServeResult(0, 1)
        }

        val fullPath = "$baseUrl/$basePath/$hashPath"
        val downloadUrl = client.getFileDownloadLink(fullPath)

        call.respondRedirect(downloadUrl)

        val size = files[hashPath.substringAfterLast('/')]?.size ?: 0
        return ServeResult(size, 1)
    }

    protected fun Sardine.getFileDownloadLink(path: String): String {
        return path
    }
}
