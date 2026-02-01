package com.bangbang93.openbmclapi

import com.bangbang93.openbmclapi.agent.AppModule
import com.bangbang93.openbmclapi.agent.config.ClusterConfig
import com.bangbang93.openbmclapi.agent.model.Counters
import com.bangbang93.openbmclapi.agent.module as appModule
import com.bangbang93.openbmclapi.agent.nat.NatService
import com.bangbang93.openbmclapi.agent.service.BootstrapService
import com.bangbang93.openbmclapi.agent.service.ClusterService
import com.bangbang93.openbmclapi.agent.storage.IStorage
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.ksp.generated.module

class ApplicationTest :
    DescribeSpec({
        beforeEach {
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

        afterEach {
            // 清理全局容器，防止测试间相互影响
            if (GlobalContext.getOrNull() != null) stopKoin()
        }

        describe("应用初始化和路由") {
            it("主页应返回 OK 状态") {
                testApplication {
                    application { this@application.appModule() }

                    client.get("/").apply {
                        status shouldBe HttpStatusCode.OK
                        val responseText = bodyAsText()
                        responseText.contains("OpenBMCLAPI") shouldBe true
                    }
                }
            }

            it("应用基础路由配置正确") {
                testApplication {
                    application { this@application.appModule() }

                    client.get("/").apply { status shouldBe HttpStatusCode.OK }
                }
            }
        }
    })
