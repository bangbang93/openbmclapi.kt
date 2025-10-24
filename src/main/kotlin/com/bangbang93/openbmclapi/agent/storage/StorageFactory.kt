package com.bangbang93.openbmclapi.agent.storage

import com.bangbang93.openbmclapi.agent.config.ClusterConfig
import org.koin.core.annotation.Single
import java.io.File

@Single
class StorageFactory(private val config: ClusterConfig) {
    fun createStorage(): IStorage {
        return when (config.storage) {
            "file" -> FileStorage(File(System.getProperty("user.dir"), "cache").absolutePath)
            else -> throw IllegalStateException("Unsupported storage type: ${config.storage}")
        }
    }
}

@Single
fun provideStorage(factory: StorageFactory): IStorage = factory.createStorage()
