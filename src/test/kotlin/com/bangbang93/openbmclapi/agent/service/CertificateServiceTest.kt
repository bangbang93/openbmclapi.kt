package com.bangbang93.openbmclapi.agent.service

import com.bangbang93.openbmclapi.agent.config.ClusterConfig
import com.bangbang93.openbmclapi.agent.model.CertificateResponse
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.nio.file.Paths
import kotlin.io.path.readText
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CertificateServiceTest {
    @Test
    fun `BYOC模式下未提供证书时返回false`() =
        runBlocking {
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
    fun `BYOC模式下提供证书时返回true并保存证书`() =
        runBlocking {
            // Arrange
            val testCert = "-----BEGIN CERTIFICATE-----\ntest cert\n-----END CERTIFICATE-----"
            val testKey = "-----BEGIN PRIVATE KEY-----\ntest key\n-----END PRIVATE KEY-----"

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
            val result = certService.setupCertificates()

            // Assert
            assertTrue(result, "Should return true when BYOC is enabled with certificates")
            assertTrue(certService.areCertificatesReady(), "Certificates should be ready")

            // Verify content
            val savedCert = Paths.get(certService.getCertificatePath()).readText()
            val savedKey = Paths.get(certService.getKeyPath()).readText()
            assertEquals(testCert, savedCert)
            assertEquals(testKey, savedKey)
        }

    @Test
    fun `非BYOC模式下请求证书并保存`() =
        runBlocking {
            // Arrange
            val remoteCert = "-----BEGIN CERTIFICATE-----\nremote cert\n-----END CERTIFICATE-----"
            val remoteKey = "-----BEGIN PRIVATE KEY-----\nremote key\n-----END PRIVATE KEY-----"

            val config =
                ClusterConfig(
                    clusterId = "test",
                    clusterSecret = "secret",
                    byoc = false,
                )
            val clusterService = mockk<ClusterService>()
            coEvery { clusterService.requestCert() } returns
                CertificateResponse(
                    cert = remoteCert,
                    key = remoteKey,
                )

            val certService = CertificateService(config, clusterService)

            // Act
            val result = certService.setupCertificates()

            // Assert
            assertTrue(result, "Should return true when certificates are fetched")
            assertTrue(certService.areCertificatesReady(), "Certificates should be ready")

            // Verify content
            val savedCert = Paths.get(certService.getCertificatePath()).readText()
            val savedKey = Paths.get(certService.getKeyPath()).readText()
            assertEquals(remoteCert, savedCert)
            assertEquals(remoteKey, savedKey)
        }

    @Test
    fun `证书路径应该在临时目录中`() {
        // Arrange
        val config =
            ClusterConfig(
                clusterId = "test",
                clusterSecret = "secret",
                byoc = false,
            )
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
