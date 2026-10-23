package com.connectapp.core.crypto

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class PrimitivesTest {

    // RFC 7914 §11 test vectors for PBKDF2-HMAC-SHA256 (also cross-checked with Python hashlib).
    @Test
    fun `pbkdf2HmacSha256 matches RFC 7914 vector with 1 iteration`() {
        val derived = pbkdf2HmacSha256("passwd", "salt".encodeToByteArray(), iterations = 1, keyLengthBytes = 64)

        assertEquals(
            "55ac046e56e3089fec1691c22544b605f94185216dde0465e68b9d57c20dacbc" +
                "49ca9cccf179b645991664b39d77ef317c71b845b1e30bd509112041d3a19783",
            toHex(derived),
        )
    }

    @Test
    fun `pbkdf2HmacSha256 matches RFC 7914 vector with 80000 iterations`() {
        val derived = pbkdf2HmacSha256("Password", "NaCl".encodeToByteArray(), iterations = 80_000, keyLengthBytes = 64)

        assertEquals(
            "4ddcd8f60b98be21830cee5ef22701f9641a4418d04c0414aeff08876b34ab56" +
                "a1d425a1225833549adb841b51c9b3176a272bdebba1d078478f62b397f33c8d",
            toHex(derived),
        )
    }

    // Non-ASCII passwords must be encoded as UTF-8 identically on every platform.
    @Test
    fun `pbkdf2HmacSha256 encodes non-ASCII passwords as UTF-8`() {
        val derived = pbkdf2HmacSha256(
            "contraseña",
            "0123456789abcdef".encodeToByteArray(),
            iterations = 1_000,
            keyLengthBytes = 32,
        )

        assertEquals("41bf4420151c3fccb8b3814332acd83f3cbac95b385cfd1f6b5f2362c1190106", toHex(derived))
    }

    @Test
    fun `normalizeNfc turns a decomposed n-tilde into the precomposed one`() {
        val decomposed = "contraseña"

        assertNotEquals("contraseña", decomposed)
        assertEquals("contraseña", normalizeNfc(decomposed))
    }

    @Test
    fun `secureRandomBytes returns the requested size and differs between calls`() {
        val a = secureRandomBytes(32)
        val b = secureRandomBytes(32)

        assertEquals(32, a.size)
        assertEquals(0, secureRandomBytes(0).size)
        assertFalse(a.contentEquals(b), "two 256-bit random values must not collide")
    }

    @Test
    fun `constantTimeEquals compares content and length`() {
        assertTrue(constantTimeEquals(byteArrayOf(1, 2, 3), byteArrayOf(1, 2, 3)))
        assertFalse(constantTimeEquals(byteArrayOf(1, 2, 3), byteArrayOf(1, 2, 4)))
        assertFalse(constantTimeEquals(byteArrayOf(1, 2, 3), byteArrayOf(1, 2)))
        assertFalse(constantTimeEquals(byteArrayOf(1, 2), byteArrayOf(1, 2, 0)))
        assertTrue(constantTimeEquals(byteArrayOf(), byteArrayOf()))
    }

    @Test
    fun `toHex renders lower-case pairs including leading zeros`() {
        assertEquals("00ff0a7f80", toHex(byteArrayOf(0, -1, 10, 127, -128)))
    }
}
