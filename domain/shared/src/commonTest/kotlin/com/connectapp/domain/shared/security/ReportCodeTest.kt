package com.connectapp.domain.shared.security

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ReportCodeTest {

    @Test
    fun `a code is the prefix and eight unambiguous symbols`() {
        val code = ReportCode.fromRandomBytes(ByteArray(ReportCode.RANDOM_LENGTH) { (it * 37).toByte() })

        assertTrue(code.startsWith("INF-"))
        assertEquals(ReportCode.PREFIX.length + ReportCode.RANDOM_LENGTH, code.length)
        assertTrue(code.removePrefix("INF-").all { it in RecoveryCode.ALPHABET })
    }

    @Test
    fun `queries are upper-cased without spaces`() {
        assertEquals("INF-7K3M", ReportCode.normalizeQuery(" inf-7k 3m "))
    }
}
