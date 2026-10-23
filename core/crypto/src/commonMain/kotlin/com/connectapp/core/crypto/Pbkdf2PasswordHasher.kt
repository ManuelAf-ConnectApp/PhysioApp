package com.connectapp.core.crypto

import kotlin.io.encoding.Base64

/**
 * Password hashing with PBKDF2-HMAC-SHA256 and a per-password random salt
 * (docs/00-auditoria-y-plan.md, D16 — confirmed 2026-10-16).
 *
 * The stored value is self-describing so the work factor can be raised later without
 * invalidating existing hashes: `pbkdf2_sha256$<iterations>$<salt base64>$<hash base64>`.
 * [needsRehash] tells callers when a successfully verified password should be re-hashed with the
 * current parameters.
 */
class Pbkdf2PasswordHasher(
    private val iterations: Int = DEFAULT_ITERATIONS,
    private val saltLengthBytes: Int = SALT_LENGTH_BYTES,
    private val hashLengthBytes: Int = HASH_LENGTH_BYTES,
    private val randomBytes: (Int) -> ByteArray = ::secureRandomBytes,
) {
    init {
        require(iterations > 0) { "iterations must be positive" }
        require(saltLengthBytes >= MIN_SALT_LENGTH_BYTES) { "salt must be at least $MIN_SALT_LENGTH_BYTES bytes" }
        require(hashLengthBytes >= MIN_HASH_LENGTH_BYTES) { "hash must be at least $MIN_HASH_LENGTH_BYTES bytes" }
    }

    fun hash(password: String): String {
        val salt = randomBytes(saltLengthBytes)
        val hash = derive(password, salt, iterations, hashLengthBytes)
        return listOf(ALGORITHM_ID, iterations.toString(), Base64.encode(salt), Base64.encode(hash))
            .joinToString(SEPARATOR)
    }

    /** False for a wrong password and for any malformed or unsupported [encoded] value. */
    fun verify(password: String, encoded: String): Boolean {
        val parsed = parse(encoded) ?: return false
        val candidate = derive(password, parsed.salt, parsed.iterations, parsed.hash.size)
        return constantTimeEquals(candidate, parsed.hash)
    }

    /** True when [encoded] was produced with weaker parameters than this hasher's. */
    fun needsRehash(encoded: String): Boolean {
        val parsed = parse(encoded) ?: return true
        return parsed.iterations < iterations || parsed.hash.size < hashLengthBytes
    }

    private fun derive(password: String, salt: ByteArray, iterations: Int, length: Int): ByteArray =
        pbkdf2HmacSha256(normalizeNfc(password), salt, iterations, length)

    private class Parsed(val iterations: Int, val salt: ByteArray, val hash: ByteArray)

    private fun parse(encoded: String): Parsed? {
        val parts = encoded.split(SEPARATOR).takeIf { it.size == PARTS && it[0] == ALGORITHM_ID }
        val parsedIterations = parts?.get(1)?.toIntOrNull()?.takeIf { it > 0 }
        val salt = parts?.get(2)?.decodeBase64OrNull()?.takeIf { it.isNotEmpty() }
        val hash = parts?.get(3)?.decodeBase64OrNull()?.takeIf { it.isNotEmpty() }
        return if (parsedIterations != null && salt != null && hash != null) {
            Parsed(parsedIterations, salt, hash)
        } else {
            null
        }
    }

    private fun String.decodeBase64OrNull(): ByteArray? = runCatching { Base64.decode(this) }.getOrNull()

    companion object {
        /** OWASP Password Storage Cheat Sheet recommendation for PBKDF2-HMAC-SHA256. */
        const val DEFAULT_ITERATIONS = 600_000
        const val SALT_LENGTH_BYTES = 16
        const val HASH_LENGTH_BYTES = 32
        private const val MIN_SALT_LENGTH_BYTES = 16
        private const val MIN_HASH_LENGTH_BYTES = 32
        private const val ALGORITHM_ID = "pbkdf2_sha256"
        private const val SEPARATOR = "$"
        private const val PARTS = 4
    }
}
