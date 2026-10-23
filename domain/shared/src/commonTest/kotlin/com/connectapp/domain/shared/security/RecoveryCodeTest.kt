package com.connectapp.domain.shared.security

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RecoveryCodeTest {

    @Test
    fun `the alphabet has 32 distinct symbols without look-alike characters`() {
        assertEquals(32, RecoveryCode.ALPHABET.toSet().size)
        listOf('0', 'O', '1', 'I').forEach { assertFalse(it in RecoveryCode.ALPHABET) }
    }

    @Test
    fun `every byte value maps to a symbol of the alphabet with no bias`() {
        val counts = IntArray(RecoveryCode.ALPHABET.length)
        (0 until 256).chunked(RecoveryCode.LENGTH).filter { it.size == RecoveryCode.LENGTH }.forEach { chunk ->
            RecoveryCode.fromRandomBytes(ByteArray(chunk.size) { chunk[it].toByte() }).forEach { symbol ->
                counts[RecoveryCode.ALPHABET.indexOf(symbol)]++
            }
        }
        // 240 bytes (12 full codes) over 32 symbols: 7 or 8 occurrences each, never 0.
        assertTrue(counts.all { it in 7..8 })
    }

    @Test
    fun `a code needs exactly LENGTH random bytes`() {
        assertFailsWith<IllegalArgumentException> { RecoveryCode.fromRandomBytes(ByteArray(19)) }
    }

    @Test
    fun `codes are accepted in any case and with or without separators`() {
        val code = RecoveryCode.fromRandomBytes(ByteArray(RecoveryCode.LENGTH) { it.toByte() })
        val formatted = RecoveryCode.format(code)

        assertEquals("23456-789AB-CDEFG-HJKLM", formatted)
        assertEquals(code, RecoveryCode.normalize(formatted.lowercase()))
        assertEquals(code, RecoveryCode.normalize(" 23456 789ab-cdefg hjklm "))
        assertTrue(RecoveryCode.isWellFormed(formatted.lowercase()))
    }

    @Test
    fun `malformed codes are rejected`() {
        assertFalse(RecoveryCode.isWellFormed(""))
        assertFalse(RecoveryCode.isWellFormed("23456-789AB-CDEFG-HJKL"))
        assertFalse(RecoveryCode.isWellFormed("23456-789AB-CDEFG-HJKLMN"))
        assertFalse(RecoveryCode.isWellFormed("O3456-789AB-CDEFG-HJKLM"))
    }
}
