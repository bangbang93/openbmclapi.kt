package com.bangbang93.openbmclapi.agent.storage

import com.aliyun.oss.OSS
import com.aliyun.oss.OSSClientBuilder
import com.aliyun.oss.model.DeleteObjectsRequest
import com.aliyun.oss.model.GeneratePresignedUrlRequest
import com.aliyun.oss.model.ListObjectsRequest
import com.aliyun.oss.model.ObjectMetadata
import com.aliyun.oss.model.ResponseHeaderOverrides
import com.bangbang93.openbmclapi.agent.model.FileInfo
import com.bangbang93.openbmclapi.agent.model.GCCounter
import com.bangbang93.openbmclapi.agent.storage.config.OssStorageConfig
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import java.io.ByteArrayInputStream
import java.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val logger = KotlinLogging.logger {}

class OssStorage(configMap: Map<String, String>) : IStorage {
  private val config: OssStorageConfig = OssStorageConfig.fromMap(configMap)
  private val client: OSS
  private val bucket: String
  private val prefix: String
  private val proxy: Boolean
  private val files = mutableMapOf<String, FileMetadata>()

  data class FileMetadata(val size: Long, val path: String)

  init {
    bucket = config.bucket
    prefix = config.prefix
    proxy = config.proxy

    client = OSSClientBuilder().build(config.endpoint, config.accessKeyId, config.accessKeySecret)
  }

  override suspend fun check(): Boolean =
      withContext(Dispatchers.IO) {
        try {
          val checkPath = if (prefix.isEmpty()) ".check" else "$prefix/.check"
          val content = System.currentTimeMillis().toString().toByteArray()
          val metadata = ObjectMetadata()
          metadata.contentLength = content.size.toLong()

          client.putObject(bucket, checkPath, ByteArrayInputStream(content), metadata)
          true
        } catch (e: Exception) {
          logger.error(e) { "Storage check failed" }
          false
        } finally {
          try {
            val checkPath = if (prefix.isEmpty()) ".check" else "$prefix/.check"
            client.deleteObject(bucket, checkPath)
          } catch (e: Exception) {
            logger.warn(e) { "Failed to delete check file" }
          }
        }
      }

  override suspend fun writeFile(path: String, content: ByteArray, fileInfo: FileInfo) =
      withContext(Dispatchers.IO) {
        val objectPath = if (prefix.isEmpty()) path else "$prefix/$path"
        val metadata = ObjectMetadata()
        metadata.contentLength = content.size.toLong()

        client.putObject(bucket, objectPath, ByteArrayInputStream(content), metadata)
        files[fileInfo.hash] = FileMetadata(content.size.toLong(), fileInfo.path)
      }

  override suspend fun exists(path: String): Boolean =
      withContext(Dispatchers.IO) {
        try {
          val objectPath = if (prefix.isEmpty()) path else "$prefix/$path"
          client.doesObjectExist(bucket, objectPath)
        } catch (e: Exception) {
          false
        }
      }

  override suspend fun getMissingFiles(files: List<FileInfo>): List<FileInfo> =
      withContext(Dispatchers.IO) {
        val remoteFileMap = files.associateBy { it.hash }.toMutableMap()

        if (this@OssStorage.files.isNotEmpty()) {
          for (hash in this@OssStorage.files.keys) {
            remoteFileMap.remove(hash)
          }
          return@withContext remoteFileMap.values.toList()
        }

        try {
          var listing =
              client.listObjects(ListObjectsRequest(bucket).withPrefix(prefix).withMaxKeys(1000))

          while (true) {
            for (objectSummary in listing.objectSummaries) {
              val objectName = objectSummary.key
              val hash =
                  if (prefix.isEmpty()) {
                    objectName.substringAfterLast('/')
                  } else {
                    objectName.removePrefix("$prefix/").substringAfterLast('/')
                  }

              val file = remoteFileMap[hash]
              if (file != null && file.size == objectSummary.size) {
                this@OssStorage.files[hash] = FileMetadata(objectSummary.size, objectName)
                remoteFileMap.remove(hash)
              }
            }

            if (!listing.isTruncated) break
            listing =
                client.listObjects(
                    ListObjectsRequest(bucket)
                        .withPrefix(prefix)
                        .withMarker(listing.nextMarker)
                        .withMaxKeys(1000)
                )
          }
        } catch (e: Exception) {
          logger.error(e) { "Failed to list OSS objects" }
        }

        remoteFileMap.values.toList()
      }

  override suspend fun gc(files: List<FileInfo>): GCCounter =
      withContext(Dispatchers.IO) {
        val fileSet = files.map { it.hash }.toSet()
        var count = 0
        var size = 0L

        try {
          var listing =
              client.listObjects(ListObjectsRequest(bucket).withPrefix(prefix).withMaxKeys(1000))

          while (true) {
            val keysToDelete = mutableListOf<String>()

            for (objectSummary in listing.objectSummaries) {
              val objectName = objectSummary.key
              val hash =
                  if (prefix.isEmpty()) {
                    objectName.substringAfterLast('/')
                  } else {
                    objectName.removePrefix("$prefix/").substringAfterLast('/')
                  }

              if (!fileSet.contains(hash)) {
                logger.info { "Deleting expired file: $objectName" }
                keysToDelete.add(objectName)
                this@OssStorage.files.remove(hash)
                count++
                size += objectSummary.size
              }
            }

            if (keysToDelete.isNotEmpty()) {
              client.deleteObjects(DeleteObjectsRequest(bucket).withKeys(keysToDelete))
            }

            if (!listing.isTruncated) break
            listing =
                client.listObjects(
                    ListObjectsRequest(bucket)
                        .withPrefix(prefix)
                        .withMarker(listing.nextMarker)
                        .withMaxKeys(1000)
                )
          }
        } catch (e: Exception) {
          logger.error(e) { "Failed to GC OSS objects" }
        }

        GCCounter(count, size)
      }

  override suspend fun serveFile(
      hashPath: String,
      call: ApplicationCall,
      name: String?,
  ): ServeResult {
    val objectPath = if (prefix.isEmpty()) hashPath else "$prefix/$hashPath"

    if (proxy) {
      // Stream the file through our server
      withContext(Dispatchers.IO) {
        try {
          val obj = client.getObject(bucket, objectPath)
          val content = obj.objectContent.readBytes()
          call.respond(HttpStatusCode.OK, content)
        } catch (e: Exception) {
          logger.error(e) { "Failed to serve file: $objectPath" }
          call.respond(HttpStatusCode.NotFound)
        }
      }
    } else {
      // Generate a presigned URL
      val url =
          withContext(Dispatchers.IO) {
            val expiration = Date(System.currentTimeMillis() + 60 * 1000)
            val request = GeneratePresignedUrlRequest(bucket, objectPath)
            request.expiration = expiration

            if (name != null) {
              val headers = ResponseHeaderOverrides()
              headers.contentDisposition = "attachment; filename=\"$name\""
              request.responseHeaders = headers
            }

            client.generatePresignedUrl(request).toString()
          }

      call.respondRedirect(url)
    }

    val size = files[hashPath.substringAfterLast('/')]?.size ?: 0
    return ServeResult(size, 1)
  }
}
