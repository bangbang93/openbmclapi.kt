package com.bangbang93.openbmclapi

import com.bangbang93.openbmclapi.agent.AppModule
import com.bangbang93.openbmclapi.agent.config.ClusterConfig
import com.bangbang93.openbmclapi.agent.model.Counters
import com.bangbang93.openbmclapi.agent.module as appModule
import com.bangbang93.openbmclapi.agent.storage.IStorage
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import io.mockk.coEvery
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
    // 启动全局 Koin 容器（与当前初始化职责一致：main 启动，测试手动启动）
    startKoin {
      modules(
          AppModule().module,
          module {
            single {
              ClusterConfig(
                  clusterId = "test-cluster",
                  clusterSecret = "test-secret",
              )
            }
            single<IStorage> {
              mockk<IStorage> {
                coEvery { check() } returns true
                coEvery { init() } returns Unit
              }
            }
            single { Counters() }
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
    // Arrange：直接调用真实 module()（suspend），以扩展接收者形式传入 Application
    application { runBlocking { this@application.appModule() } }

    // Act
    client.get("/").apply {
      // Assert
      assertEquals(HttpStatusCode.OK, status)
      val responseText = bodyAsText()
      assertTrue(responseText.contains("OpenBMCLAPI"))
    }
  }
}
