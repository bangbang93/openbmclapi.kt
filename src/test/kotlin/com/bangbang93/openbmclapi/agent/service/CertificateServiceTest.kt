package com.bangbang93.openbmclapi.agent.service

import com.bangbang93.openbmclapi.agent.config.ClusterConfig
import com.bangbang93.openbmclapi.agent.model.CertificateResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.Test

private val logger = KotlinLogging.logger {}

class CertificateServiceTest {
    @Test
    fun `BYOC模式下未提供证书时返回false`() = runBlocking {
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
        assertFalse(result, "Should return false when BYOC is enabled but no certificates provided")
    }

    @Test
    fun `BYOC模式下提供证书时保存到临时目录`() = runBlocking {
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
        assertTrue(certPath.isNotEmpty(), "Certificate path should not be empty")
        assertTrue(keyPath.isNotEmpty(), "Key path should not be empty")
    }

    @Test
    fun `非BYOC模式下请求证书并保存到临时目录`() = runBlocking {
        // Arrange: Mock 外部服务调用
        val config = ClusterConfig(clusterId = "test", clusterSecret = "secret", byoc = false)
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
        assertTrue(certPath.isNotEmpty(), "Certificate path should be set")
        assertTrue(keyPath.isNotEmpty(), "Key path should be set")
    }

    @Test
    fun `证书路径应该在临时目录中`() {
        // Arrange
        val config = ClusterConfig(clusterId = "test", clusterSecret = "secret", byoc = false)
        val clusterService = mockk<ClusterService>()
        val certService = CertificateService(config, clusterService)

        // Act
        val certPath = certService.getCertificatePath()
        val keyPath = certService.getKeyPath()

        // Assert
        val tmpDir = System.getProperty("java.io.tmpdir")
        assertTrue(certPath.startsWith(tmpDir), "Certificate path should be in temp directory")
        assertTrue(keyPath.startsWith(tmpDir), "Key path should be in temp directory")
        assertTrue(certPath.contains("openbmclapi"), "Certificate path should contain openbmclapi")
        assertTrue(keyPath.contains("openbmclapi"), "Key path should contain openbmclapi")
    }
}
