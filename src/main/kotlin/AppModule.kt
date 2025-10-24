package com.bangbang93.openbmclapi

import com.bangbang93.openbmclapi.config.ClusterConfig
import com.bangbang93.openbmclapi.model.Counters
import com.bangbang93.openbmclapi.service.BootstrapService
import com.bangbang93.openbmclapi.service.ClusterService
import com.bangbang93.openbmclapi.service.TokenManager
import com.bangbang93.openbmclapi.storage.FileStorage
import com.bangbang93.openbmclapi.storage.IStorage
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import java.io.File

@Module
@ComponentScan("com.bangbang93.openbmclapi")
class AppModule

@Single
fun provideStorage(config: ClusterConfig): IStorage {
    return when (config.storage) {
        "file" -> FileStorage(File(System.getProperty("user.dir"), "cache").absolutePath)
        else -> throw IllegalStateException("Unsupported storage type: ${config.storage}")
    }
}

@Single
fun provideTokenManager(config: ClusterConfig): TokenManager {
    val version = System.getProperty("app.version") ?: "0.0.1"
    return TokenManager(
        config.clusterId,
        config.clusterSecret,
        version,
        config.clusterBmclapi,
    )
}

@Single
fun provideClusterService(
    config: ClusterConfig,
    storage: IStorage,
    tokenManager: TokenManager,
    counters: Counters,
): ClusterService {
    val version = System.getProperty("app.version") ?: "0.0.1"
    return ClusterService(
        config,
        storage,
        tokenManager,
        counters,
        version,
    )
}

@Single
fun provideBootstrapService(
    config: ClusterConfig,
    storage: IStorage,
    tokenManager: TokenManager,
    clusterService: ClusterService,
    counters: Counters,
): BootstrapService {
    val version = System.getProperty("app.version") ?: "0.0.1"
    return BootstrapService(
        config,
        storage,
        tokenManager,
        clusterService,
        counters,
        version,
    )
}

@Single
fun provideCounters(): Counters {
    return Counters()
}
