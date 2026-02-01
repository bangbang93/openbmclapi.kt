package com.bangbang93.openbmclapi.agent.storage

import com.bangbang93.openbmclapi.agent.model.FileInfo
import com.bangbang93.openbmclapi.agent.model.GCCounter
import io.ktor.server.application.ApplicationCall

interface IStorage {
  suspend fun init() {}

  suspend fun check(): Boolean

  suspend fun writeFile(
      path: String,
      content: ByteArray,
      fileInfo: FileInfo,
  )

  suspend fun exists(path: String): Boolean

  suspend fun getMissingFiles(files: List<FileInfo>): List<FileInfo>

  suspend fun gc(files: List<FileInfo>): GCCounter

  suspend fun serveFile(
      hashPath: String,
      call: ApplicationCall,
      name: String?,
  ): ServeResult
}

data class ServeResult(
    val bytes: Long,
    val hits: Long,
)
