package com.bangbang93.openbmclapi.storage

import com.bangbang93.openbmclapi.model.FileInfo
import com.bangbang93.openbmclapi.model.GCCounter
import com.bangbang93.openbmclapi.util.HashUtil
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import java.io.File
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.fileSize

class FileStorage(private val cacheDir: String) : IStorage {
    private val logger = LoggerFactory.getLogger(FileStorage::class.java)

    override suspend fun check(): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val dir = File(cacheDir)
                if (!dir.exists()) {
                    dir.mkdirs()
                }
                val checkFile = File(dir, ".check")
                checkFile.createNewFile()
                checkFile.delete()
                true
            } catch (e: Exception) {
                logger.error("Storage check failed", e)
                false
            }
        }

    override suspend fun writeFile(
        path: String,
        content: ByteArray,
        fileInfo: FileInfo,
    ) = withContext(Dispatchers.IO) {
        val file = File(cacheDir, path)
        file.parentFile?.mkdirs()
        file.writeBytes(content)
    }

    override suspend fun exists(path: String): Boolean =
        withContext(Dispatchers.IO) {
            File(cacheDir, path).exists()
        }

    override suspend fun getMissingFiles(files: List<FileInfo>): List<FileInfo> =
        withContext(Dispatchers.IO) {
            files.filter { fileInfo ->
                val path = Path(cacheDir, HashUtil.hashToFilename(fileInfo.hash))
                !path.exists() || path.fileSize() != fileInfo.size
            }
        }

    override suspend fun gc(files: List<FileInfo>): GCCounter =
        withContext(Dispatchers.IO) {
            val fileSet = files.map { HashUtil.hashToFilename(it.hash) }.toSet()
            var count = 0
            var size = 0L

            val queue = mutableListOf(File(cacheDir))
            while (queue.isNotEmpty()) {
                val dir = queue.removeAt(0)
                dir.listFiles()?.forEach { entry ->
                    if (entry.isDirectory) {
                        queue.add(entry)
                    } else {
                        val relativePath = entry.relativeTo(File(cacheDir)).path
                        if (!fileSet.contains(relativePath)) {
                            logger.info("Deleting expired file: ${entry.path}")
                            size += entry.length()
                            entry.delete()
                            count++
                        }
                    }
                }
            }

            GCCounter(count, size)
        }

    override suspend fun serveFile(
        hashPath: String,
        call: ApplicationCall,
        name: String?,
    ): ServeResult {
        val file = File(cacheDir, hashPath)

        if (!file.exists()) {
            call.respond(HttpStatusCode.NotFound)
            return ServeResult(0, 0)
        }

        if (name != null) {
            call.response.header(
                HttpHeaders.ContentDisposition,
                ContentDisposition.Attachment.withParameter(ContentDisposition.Parameters.FileName, name).toString(),
            )
        }

        call.response.header(HttpHeaders.CacheControl, "max-age=2592000") // 30 days

        val fileSize = file.length()
        call.respondFile(file)

        return ServeResult(fileSize, 1)
    }

    private fun getAbsolutePath(path: String): String {
        return File(cacheDir, path).absolutePath
    }
}
