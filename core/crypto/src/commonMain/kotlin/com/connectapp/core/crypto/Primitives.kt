package com.connectapp.core.crypto

/** [size] bytes from the platform's cryptographically secure random number generator. */
expect fun secureRandomBytes(size: Int): ByteArray

/**
 * PBKDF2 with HMAC-SHA256 (RFC 8018), computed by the platform's native implementation.
 *
 * [password] is encoded as UTF-8 exactly as given: callers that hash user-typed passwords should
 * normalize it first (see [normalizeNfc]) — [Pbkdf2PasswordHasher] does.
 */
expect fun pbkdf2HmacSha256(password: String, salt: ByteArray, iterations: Int, keyLengthBytes: Int): ByteArray

/**
 * Unicode NFC normalization, so the same password typed on different keyboards (e.g. "ñ" as one
 * precomposed code point or as "n" + combining tilde) always derives the same key.
 */
expect fun normalizeNfc(value: String): String

/** Compares two byte arrays in time independent of where they differ (no early exit). */
fun constantTimeEquals(a: ByteArray, b: ByteArray): Boolean {
    var diff = a.size xor b.size
    for (i in 0 until maxOf(a.size, b.size)) {
        diff = diff or (a.getOrElse(i) { 0 }.toInt() xor b.getOrElse(i) { 0 }.toInt())
    }
    return diff == 0
}

private const val HEX_DIGITS = "0123456789abcdef"

/** Lower-case hexadecimal representation of [bytes]. */
fun toHex(bytes: ByteArray): String = buildString(bytes.size * 2) {
    bytes.forEach { byte ->
        val value = byte.toInt() and 0xFF
        append(HEX_DIGITS[value ushr 4])
        append(HEX_DIGITS[value and 0x0F])
    }
}
