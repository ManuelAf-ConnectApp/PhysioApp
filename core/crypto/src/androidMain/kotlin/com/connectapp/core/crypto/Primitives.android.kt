package com.connectapp.core.crypto

import java.security.SecureRandom
import java.text.Normalizer
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

private val secureRandom = SecureRandom()

actual fun secureRandomBytes(size: Int): ByteArray = ByteArray(size).also(secureRandom::nextBytes)

// PBKDF2WithHmacSHA256 encodes the char[] password as UTF-8, matching the iOS implementation.
actual fun pbkdf2HmacSha256(password: String, salt: ByteArray, iterations: Int, keyLengthBytes: Int): ByteArray {
    val chars = password.toCharArray()
    val spec = PBEKeySpec(chars, salt, iterations, keyLengthBytes * Byte.SIZE_BITS)
    return try {
        SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
    } finally {
        spec.clearPassword()
        chars.fill('\u0000')
    }
}

actual fun normalizeNfc(value: String): String = Normalizer.normalize(value, Normalizer.Form.NFC)
