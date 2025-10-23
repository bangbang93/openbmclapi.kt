package com.bangbang93.openbmclapi.util

import java.io.File
import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object HashUtil {
    fun hashToFilename(hash: String): String {
        return "${hash.substring(0, 2)}${File.separator}$hash"
    }

    fun validateFile(buffer: ByteArray, checkSum: String): Boolean {
        val algorithm = if (checkSum.length == 32) "MD5" else "SHA-1"
        val digest = MessageDigest.getInstance(algorithm)
        val hash = digest.digest(buffer)
        return hash.toHexString() == checkSum
    }

    fun checkSign(hash: String, secret: String, query: Map<String, String>): Boolean {
        val s = query["s"] ?: return false
        val e = query["e"] ?: return false

        val mac = Mac.getInstance("HmacSHA1")
        val secretKey = SecretKeySpec(secret.toByteArray(), "HmacSHA1")
        mac.init(secretKey)
        
        val toSign = "$secret$hash$e"
        mac.update(toSign.toByteArray())
        val sign = mac.doFinal().toBase64Url()

        val expiryTime = e.toLongOrNull(36) ?: return false
        return sign == s && System.currentTimeMillis() < expiryTime
    }

    fun createHmacSha256(secret: String, data: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        val secretKey = SecretKeySpec(secret.toByteArray(), "HmacSHA256")
        mac.init(secretKey)
        val hash = mac.doFinal(data.toByteArray())
        return hash.toHexString()
    }

    private fun ByteArray.toHexString(): String {
        return joinToString("") { "%02x".format(it) }
    }

    private fun ByteArray.toBase64Url(): String {
        val base64 = java.util.Base64.getUrlEncoder().withoutPadding()
        return base64.encodeToString(this)
    }
}
