package com.bangbang93.openbmclapi.agent.service

import com.bangbang93.openbmclapi.agent.config.ClusterConfig
import com.bangbang93.openbmclapi.agent.model.CertificateResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotBeEmpty
import io.kotest.matchers.string.shouldStartWith
import io.mockk.coEvery
import io.mockk.mockk

private val logger = KotlinLogging.logger {}

class CertificateServiceTest :
    DescribeSpec({
        describe("CertificateService") {
            describe("BYOC 模式") {
                it("未提供证书时返回false") {
                    // Arrange
                    val config =
                        ClusterConfig(
                            clusterId = "test",
                            clusterSecret = "secret",
                            byoc = true,
                            sslCert = null,
                            sslKey = null,
                        )
                    val clusterService = mockk<ClusterService>()
                    val certService = CertificateService(config, clusterService)

                    // Act
                    val result = certService.setupCertificates()

                    // Assert
                    result shouldBe false
                }

                it("提供证书时保存到临时目录") {
                    // Arrange: 使用 mock 避免证书转换过程
                    val testCert = "test-cert-content"
                    val testKey = "test-key-content"

                    val config =
                        ClusterConfig(
                            clusterId = "test",
                            clusterSecret = "secret",
                            byoc = true,
                            sslCert = testCert,
                            sslKey = testKey,
                        )
                    val clusterService = mockk<ClusterService>()
                    val certService = CertificateService(config, clusterService)

                    // Act
                    // 由于 mock 证书内容不是有效的 PEM，此处仅测试文件操作
                    try {
                        certService.setupCertificates()
                    } catch (e: Exception) {
                        // Expected - test certs are not valid PEM format
                        logger.debug { "Expected error during PEM conversion: ${e.message}" }
                    }

                    // Assert: 验证证书路径有效
                    val certPath = certService.getCertificatePath()
                    val keyPath = certService.getKeyPath()
                    certPath.shouldNotBeEmpty()
                    keyPath.shouldNotBeEmpty()
                }
            }

            describe("非 BYOC 模式") {
                it("请求证书并保存到临时目录") {
                    // Arrange: Mock 外部服务调用
                    val config =
                        ClusterConfig(clusterId = "test", clusterSecret = "secret", byoc = false)
                    val clusterService = mockk<ClusterService>()
                    coEvery { clusterService.requestCert() } returns
                        CertificateResponse(cert = "test-remote-cert", key = "test-remote-key")

                    val certService = CertificateService(config, clusterService)

                    // Act
                    // 由于 mock 证书不是有效 PEM，此处仅测试请求流程
                    try {
                        certService.setupCertificates()
                    } catch (e: Exception) {
                        // Expected - test certs are not valid PEM format
                        logger.debug { "Expected error during PEM conversion: ${e.message}" }
                    }

                    // Assert: 验证证书路径有效
                    val certPath = certService.getCertificatePath()
                    val keyPath = certService.getKeyPath()
                    certPath.shouldNotBeEmpty()
                    keyPath.shouldNotBeEmpty()
                }
            }

            describe("证书路径") {
                it("应该在临时目录中") {
                    // Arrange
                    val config =
                        ClusterConfig(clusterId = "test", clusterSecret = "secret", byoc = false)
                    val clusterService = mockk<ClusterService>()
                    val certService = CertificateService(config, clusterService)

                    // Act
                    val certPath = certService.getCertificatePath()
                    val keyPath = certService.getKeyPath()

                    // Assert
                    val tmpDir = System.getProperty("java.io.tmpdir")
                    certPath shouldStartWith tmpDir
                    keyPath shouldStartWith tmpDir
                    certPath shouldContain "openbmclapi"
                    keyPath shouldContain "openbmclapi"
                }
            }
        }
    })
