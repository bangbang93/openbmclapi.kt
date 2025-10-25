package com.bangbang93.openbmclapi.agent.service

import com.bangbang93.openbmclapi.agent.config.ClusterConfig
import io.github.oshai.kotlinlogging.KotlinLogging
import org.koin.core.annotation.Single
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

private val logger = KotlinLogging.logger {}

@Single
class CertificateService(
    private val config: ClusterConfig,
    private val clusterService: ClusterService,
) {
    private val tmpDir: Path = Paths.get(System.getProperty("java.io.tmpdir"), "openbmclapi")
    private val certPath: Path = tmpDir.resolve("cert.pem")
    private val keyPath: Path = tmpDir.resolve("key.pem")

    init {
        tmpDir.createDirectories()
    }

    /**
     * Setup SSL certificates based on BYOC configuration.
     * Returns true if HTTPS should be used, false otherwise.
     */
    suspend fun setupCertificates(): Boolean {
        return if (config.byoc) {
            // BYOC mode - use local certificates if provided
            if (config.sslCert.isNullOrBlank() || config.sslKey.isNullOrBlank()) {
                logger.info { "BYOC mode enabled but no certificates provided, using HTTP" }
                false
            } else {
                logger.info { "BYOC mode: using local certificates" }
                useSelfCert()
                true
            }
        } else {
            // Request certificates from server
            logger.info { "Requesting certificates from server" }
            requestCert()
            true
        }
    }

    /**
     * Request certificate from remote server via socket.io
     */
    private suspend fun requestCert() {
        val certResponse = clusterService.requestCert()

        // Write certificate and key to temp files
        certPath.writeText(certResponse.cert)
        keyPath.writeText(certResponse.key)

        logger.info { "Certificates received and saved to temporary directory" }
    }

    /**
     * Use local SSL certificate files
     */
    private fun useSelfCert() {
        val sslCert = config.sslCert ?: throw Exception("Missing SSL certificate")
        val sslKey = config.sslKey ?: throw Exception("Missing SSL key")

        // Check if the values are file paths or the actual certificate content
        val certContent =
            if (File(sslCert).exists()) {
                logger.debug { "Reading certificate from file: $sslCert" }
                Paths.get(sslCert).readText()
            } else {
                logger.debug { "Using certificate content from configuration" }
                sslCert
            }

        val keyContent =
            if (File(sslKey).exists()) {
                logger.debug { "Reading key from file: $sslKey" }
                Paths.get(sslKey).readText()
            } else {
                logger.debug { "Using key content from configuration" }
                sslKey
            }

        // Write to temp files
        certPath.writeText(certContent)
        keyPath.writeText(keyContent)

        logger.info { "Local certificates loaded and saved to temporary directory" }
    }

    /**
     * Get the path to the certificate file
     */
    fun getCertificatePath(): String = certPath.toString()

    /**
     * Get the path to the key file
     */
    fun getKeyPath(): String = keyPath.toString()

    /**
     * Check if certificates are ready
     */
    fun areCertificatesReady(): Boolean = certPath.exists() && keyPath.exists()
}
