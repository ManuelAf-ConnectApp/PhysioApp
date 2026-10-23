package com.connectapp.core.crypto

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class Pbkdf2PasswordHasherTest {

    // Low iteration count keeps the suite fast; production uses DEFAULT_ITERATIONS.
    private val hasher = Pbkdf2PasswordHasher(iterations = 1_000)

    @Test
    fun `default parameters follow the confirmed policy`() {
        assertEquals(600_000, Pbkdf2PasswordHasher.DEFAULT_ITERATIONS)
        assertEquals(16, Pbkdf2PasswordHasher.SALT_LENGTH_BYTES)
        assertEquals(32, Pbkdf2PasswordHasher.HASH_LENGTH_BYTES)
    }

    @Test
    fun `hash is self-describing and never contains the password`() {
        val encoded = hasher.hash("Fisio-Clinica#2026")
        val parts = encoded.split("$")

        assertEquals(4, parts.size)
        assertEquals("pbkdf2_sha256", parts[0])
        assertEquals("1000", parts[1])
        assertFalse(encoded.contains("Fisio-Clinica#2026"))
    }

    @Test
    fun `verify accepts the right password and rejects a wrong one`() {
        val encoded = hasher.hash("Fisio-Clinica#2026")

        assertTrue(hasher.verify("Fisio-Clinica#2026", encoded))
        assertFalse(hasher.verify("fisio-clinica#2026", encoded))
        assertFalse(hasher.verify("", encoded))
    }

    @Test
    fun `the same password hashes differently each time thanks to the random salt`() {
        assertNotEquals(hasher.hash("Fisio-Clinica#2026"), hasher.hash("Fisio-Clinica#2026"))
    }

    @Test
    fun `verify normalizes the password so composed and decomposed forms match`() {
        val encoded = hasher.hash("Contraseña-Segura1")

        assertTrue(hasher.verify("Contraseña-Segura1", encoded))
    }

    @Test
    fun `verify uses the iterations stored in the hash instead of the current setting`() {
        val encodedWithOldParams = Pbkdf2PasswordHasher(iterations = 500).hash("Fisio-Clinica#2026")

        assertTrue(hasher.verify("Fisio-Clinica#2026", encodedWithOldParams))
        assertTrue(hasher.needsRehash(encodedWithOldParams))
        assertFalse(hasher.needsRehash(hasher.hash("Fisio-Clinica#2026")))
    }

    @Test
    fun `malformed or foreign hashes never verify`() {
        listOf(
            "",
            "plain-text-password",
            "md5\$1000\$AAAA\$BBBB",
            "pbkdf2_sha256\$abc\$AAAAAAAAAAAAAAAAAAAAAA==\$AAAA",
            "pbkdf2_sha256\$0\$AAAAAAAAAAAAAAAAAAAAAA==\$AAAA",
            "pbkdf2_sha256\$1000\$not base64!\$AAAA",
            "pbkdf2_sha256\$1000\$\$",
        ).forEach { encoded ->
            assertFalse(hasher.verify("anything", encoded), "must reject '$encoded'")
            assertTrue(hasher.needsRehash(encoded))
        }
    }

    @Test
    fun `weak parameters are rejected at construction`() {
        assertFailsWith<IllegalArgumentException> { Pbkdf2PasswordHasher(iterations = 0) }
        assertFailsWith<IllegalArgumentException> { Pbkdf2PasswordHasher(saltLengthBytes = 8) }
        assertFailsWith<IllegalArgumentException> { Pbkdf2PasswordHasher(hashLengthBytes = 16) }
    }
}
