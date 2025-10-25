package com.bangbang93.openbmclapi.agent.storage

import com.bangbang93.openbmclapi.agent.model.FileInfo
import com.bangbang93.openbmclapi.agent.model.GCCounter
import com.bangbang93.openbmclapi.agent.storage.config.MinioStorageConfig
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respondRedirect
import io.minio.GetPresignedObjectUrlArgs
import io.minio.ListObjectsArgs
import io.minio.MinioClient
import io.minio.PutObjectArgs
import io.minio.RemoveObjectArgs
import io.minio.StatObjectArgs
import io.minio.http.Method
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.net.URI

private val logger = KotlinLogging.logger {}

class MinioStorage(configMap: Map<String, String>) : IStorage {
    private val config: MinioStorageConfig = MinioStorageConfig.fromMap(configMap)
    private val client: MinioClient
    private val internalClient: MinioClient
    private val bucket: String
    private val prefix: String
    private val files = mutableMapOf<String, FileMetadata>()

    data class FileMetadata(
        val size: Long,
        val path: String,
    )

    init {
        client = createMinioClient(config.url)
        internalClient =
            if (config.internalUrl != null) {
                createMinioClient(config.internalUrl)
            } else {
                client
            }

        val uri = URI.create(config.url)
        val pathParts = uri.path.split("/").filter { it.isNotEmpty() }
        bucket = pathParts.firstOrNull() ?: throw IllegalArgumentException("MinIO bucket not specified in url path")
        prefix = pathParts.drop(1).joinToString("/")
    }

    private fun createMinioClient(urlStr: String): MinioClient {
        val uri = URI.create(urlStr)
        val username = uri.userInfo?.substringBefore(':') ?: ""
        val password = uri.userInfo?.substringAfter(':') ?: ""
        val port =
            if (uri.port > 0) {
                uri.port
            } else if (uri.scheme == "https") {
                443
            } else {
                9000
            }

        return MinioClient
            .builder()
            .endpoint(uri.host, port, uri.scheme == "https")
            .credentials(username, password)
            .build()
    }

    override suspend fun check(): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val checkPath = if (prefix.isEmpty()) ".check" else "$prefix/.check"
                val content = System.currentTimeMillis().toString().toByteArray()

                internalClient.putObject(
                    PutObjectArgs
                        .builder()
                        .bucket(bucket)
                        .`object`(checkPath)
                        .stream(ByteArrayInputStream(content), content.size.toLong(), -1)
                        .build(),
                )

                client.putObject(
                    PutObjectArgs
                        .builder()
                        .bucket(bucket)
                        .`object`(checkPath)
                        .stream(ByteArrayInputStream(content), content.size.toLong(), -1)
                        .build(),
                )
                true
            } catch (e: Exception) {
                logger.error(e) { "Storage check failed" }
                false
            } finally {
                try {
                    val checkPath = if (prefix.isEmpty()) ".check" else "$prefix/.check"
                    internalClient.removeObject(
                        RemoveObjectArgs.builder().bucket(bucket).`object`(checkPath).build(),
                    )
                    client.removeObject(
                        RemoveObjectArgs.builder().bucket(bucket).`object`(checkPath).build(),
                    )
                } catch (e: Exception) {
                    logger.warn(e) { "Failed to delete check file" }
                }
            }
        }

    override suspend fun writeFile(
        path: String,
        content: ByteArray,
        fileInfo: FileInfo,
    ) = withContext(Dispatchers.IO) {
        val objectPath = if (prefix.isEmpty()) path else "$prefix/$path"

        internalClient.putObject(
            PutObjectArgs
                .builder()
                .bucket(bucket)
                .`object`(objectPath)
                .stream(ByteArrayInputStream(content), content.size.toLong(), -1)
                .build(),
        )

        files[fileInfo.hash] = FileMetadata(content.size.toLong(), fileInfo.path)
    }

    override suspend fun exists(path: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val objectPath = if (prefix.isEmpty()) path else "$prefix/$path"
                internalClient.statObject(
                    StatObjectArgs.builder().bucket(bucket).`object`(objectPath).build(),
                )
                true
            } catch (e: Exception) {
                false
            }
        }

    override suspend fun getMissingFiles(files: List<FileInfo>): List<FileInfo> =
        withContext(Dispatchers.IO) {
            val remoteFileMap = files.associateBy { it.hash }.toMutableMap()

            if (this@MinioStorage.files.isNotEmpty()) {
                for (hash in this@MinioStorage.files.keys) {
                    remoteFileMap.remove(hash)
                }
                return@withContext remoteFileMap.values.toList()
            }

            try {
                val objects =
                    internalClient.listObjects(
                        ListObjectsArgs.builder().bucket(bucket).prefix(prefix).recursive(true).build(),
                    )

                for (result in objects) {
                    val item = result.get()
                    val objectName = item.objectName()
                    val hash =
                        if (prefix.isEmpty()) {
                            objectName.substringAfterLast('/')
                        } else {
                            objectName.removePrefix("$prefix/").substringAfterLast('/')
                        }

                    val file = remoteFileMap[hash]
                    if (file != null && file.size == item.size()) {
                        this@MinioStorage.files[hash] = FileMetadata(item.size(), objectName)
                        remoteFileMap.remove(hash)
                    }
                }
            } catch (e: Exception) {
                logger.error(e) { "Failed to list MinIO objects" }
            }

            remoteFileMap.values.toList()
        }

    override suspend fun gc(files: List<FileInfo>): GCCounter =
        withContext(Dispatchers.IO) {
            val fileSet = files.map { it.hash }.toSet()
            var count = 0
            var size = 0L

            try {
                val objects =
                    internalClient.listObjects(
                        ListObjectsArgs.builder().bucket(bucket).prefix(prefix).recursive(true).build(),
                    )

                for (result in objects) {
                    val item = result.get()
                    val objectName = item.objectName()
                    val hash =
                        if (prefix.isEmpty()) {
                            objectName.substringAfterLast('/')
                        } else {
                            objectName.removePrefix("$prefix/").substringAfterLast('/')
                        }

                    if (!fileSet.contains(hash)) {
                        logger.info { "Deleting expired file: $objectName" }
                        internalClient.removeObject(
                            RemoveObjectArgs.builder().bucket(bucket).`object`(objectName).build(),
                        )
                        this@MinioStorage.files.remove(hash)
                        count++
                        size += item.size()
                    }
                }
            } catch (e: Exception) {
                logger.error(e) { "Failed to GC MinIO objects" }
            }

            GCCounter(count, size)
        }

    override suspend fun serveFile(
        hashPath: String,
        call: ApplicationCall,
        name: String?,
    ): ServeResult {
        val objectPath = if (prefix.isEmpty()) hashPath else "$prefix/$hashPath"

        val url =
            withContext(Dispatchers.IO) {
                val args =
                    GetPresignedObjectUrlArgs
                        .builder()
                        .method(Method.GET)
                        .bucket(bucket)
                        .`object`(objectPath)
                        .expiry(60)

                if (name != null) {
                    args.extraQueryParams(
                        mapOf(
                            "response-content-disposition" to "attachment; filename=\"$name\"",
                        ),
                    )
                }

                client.getPresignedObjectUrl(args.build())
            }

        call.respondRedirect(url)

        val size = files[hashPath.substringAfterLast('/')]?.size ?: 0
        return ServeResult(size, 1)
    }
}
