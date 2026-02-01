package com.bangbang93.openbmclapi.agent.util

import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.File
import java.io.FileOutputStream
import java.io.StringReader
import java.security.KeyStore
import java.security.PrivateKey
import java.security.Security
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openssl.PEMKeyPair
import org.bouncycastle.openssl.PEMParser
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter

private val logger = KotlinLogging.logger {}

object PemToKeyStoreConverter {
  init {
    // Add BouncyCastle as a security provider
    if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
      Security.addProvider(BouncyCastleProvider())
    }
  }

  /** Convert PEM certificate and key files to a JKS keystore file */
  fun convertPemToJks(
      certPath: String,
      keyPath: String,
      keystorePath: String,
      keystorePassword: String = "openbmclapi",
      alias: String = "openbmclapi",
  ) {
    try {
      // Read certificate
      val certContent = File(certPath).readText()
      val certificate = parseCertificate(certContent)

      // Read private key
      val keyContent = File(keyPath).readText()
      val privateKey = parsePrivateKey(keyContent)

      // Create keystore
      val keyStore = KeyStore.getInstance("JKS")
      keyStore.load(null, keystorePassword.toCharArray())

      // Add certificate and key to keystore
      keyStore.setKeyEntry(
          alias,
          privateKey,
          keystorePassword.toCharArray(),
          arrayOf(certificate),
      )

      // Save keystore to file
      FileOutputStream(keystorePath).use { fos ->
        keyStore.store(fos, keystorePassword.toCharArray())
      }

      logger.info { "Successfully converted PEM to JKS keystore at: $keystorePath" }
    } catch (e: Exception) {
      logger.error(e) { "Failed to convert PEM to JKS" }
      throw e
    }
  }

  private fun parseCertificate(certContent: String): Certificate {
    val certificateFactory = CertificateFactory.getInstance("X.509")
    val certInputStream = certContent.byteInputStream()
    return certificateFactory.generateCertificate(certInputStream)
  }

  private fun parsePrivateKey(keyContent: String): PrivateKey {
    val pemParser = PEMParser(StringReader(keyContent))
    val keyConverter = JcaPEMKeyConverter().setProvider("BC")

    return when (val keyObject = pemParser.readObject()) {
      is PEMKeyPair -> keyConverter.getPrivateKey(keyObject.privateKeyInfo)
      is PrivateKeyInfo -> keyConverter.getPrivateKey(keyObject)
      else -> throw IllegalArgumentException("Unsupported PEM key format")
    }
  }
}
