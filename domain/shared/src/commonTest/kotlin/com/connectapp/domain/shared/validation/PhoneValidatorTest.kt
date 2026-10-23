package com.connectapp.domain.shared.validation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PhoneValidatorTest {

    @Test
    fun `spanish and international numbers are valid`() {
        listOf("600123456", "600 12 34 56", "+34 600 123 456", "+351912345678", " 912345678 ")
            .forEach { assertTrue(PhoneValidator.isValid(it), "'$it' must be accepted") }
    }

    @Test
    fun `too short or too long or with other characters is invalid`() {
        listOf("", "12345678", "1234567890123456", "600-123-456", "(600)123456", "600 12 34 5a", "34+600123456")
            .forEach { assertFalse(PhoneValidator.isValid(it), "'$it' must be rejected") }
    }

    @Test
    fun `normalization collapses spaces`() {
        assertEquals("+34 600 123 456", PhoneValidator.normalize("  +34   600 123  456 "))
    }
}
