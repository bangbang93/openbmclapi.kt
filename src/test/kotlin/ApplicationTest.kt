package com.bangbang93.openbmclapi

import com.bangbang93.openbmclapi.agent.AppModule
import com.bangbang93.openbmclapi.agent.config.ClusterConfig
import com.bangbang93.openbmclapi.agent.model.Counters
import com.bangbang93.openbmclapi.agent.module as appModule
import com.bangbang93.openbmclapi.agent.nat.NatService
import com.bangbang93.openbmclapi.agent.service.BootstrapService
import com.bangbang93.openbmclapi.agent.service.ClusterService
import com.bangbang93.openbmclapi.agent.storage.IStorage
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.ksp.generated.module

class ApplicationTest {
    @BeforeTest
    fun setupKoin() {
        // 启动全局 Koin 容器，使用 mock 外部依赖
        startKoin {
            modules(
                AppModule().module,
                module {
                    single {
                        ClusterConfig(clusterId = "test-cluster", clusterSecret = "test-secret")
                    }
                    single<IStorage> {
                        mockk<IStorage> {
                            coEvery { check() } returns true
                            coEvery { init() } returns Unit
                        }
                    }
                    single { Counters() }
                    // Mock 外部服务依赖
                    single<ClusterService> {
                        mockk {
                            coEvery { getConfiguration() } returns mockk()
                            coEvery { getFileList() } returns mockk()
                            coEvery { getFileList(any()) } returns mockk()
                            coEvery { syncFiles(any(), any()) } returns Unit
                            coEvery { enable() } returns Unit
                            coEvery { disable() } returns Unit
                        }
                    }
                    single<NatService> { mockk { every { startIfEnabled() } returns null } }
                    // Mock BootstrapService 避免真实的 bootstrap 过程
                    single<BootstrapService> { mockk { coEvery { shutdown() } returns Unit } }
                },
            )
        }
    }

    @AfterTest
    fun tearDownKoin() {
        // 清理全局容器，防止测试间相互影响
        if (GlobalContext.getOrNull() != null) stopKoin()
    }

    @Test
    fun `主页返回OK`() = testApplication {
        // Arrange: 应用初始化，所有外部依赖已 mock
        application { runBlocking { this@application.appModule() } }

        // Act
        client.get("/").apply {
            // Assert
            assertEquals(HttpStatusCode.OK, status)
            val responseText = bodyAsText()
            assertTrue(responseText.contains("OpenBMCLAPI"))
        }
    }

    @Test
    fun `应用基础路由配置正确`() = testApplication {
        application { runBlocking { this@application.appModule() } }

        // Test basic route
        client.get("/").apply { assertEquals(HttpStatusCode.OK, status) }
    }
}
