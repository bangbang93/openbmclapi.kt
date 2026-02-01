package com.bangbang93.openbmclapi.agent.storage

import com.bangbang93.openbmclapi.agent.config.ClusterConfig
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.File
import org.koin.core.annotation.Single

private val logger = KotlinLogging.logger {}

@Single
class StorageFactory(private val config: ClusterConfig) {
  fun createStorage(): IStorage {
    val storage =
        when (config.storage) {
          "file" -> FileStorage(File(System.getProperty("user.dir"), "cache").absolutePath)
          "webdav" -> WebdavStorage(config.storageOpts)
          "alist" -> AlistWebdavStorage(config.storageOpts)
          "minio" -> MinioStorage(config.storageOpts)
          "oss" -> OssStorage(config.storageOpts)
          else -> throw IllegalStateException("Unsupported storage type: ${config.storage}")
        }
    logger.info { "Using storage type: ${config.storage}" }
    return storage
  }
}

@Single fun provideStorage(factory: StorageFactory): IStorage = factory.createStorage()
