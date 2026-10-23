package com.connectapp.domain.shared.validation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// Fictitious examples.
class LicenseNumberValidatorTest {

    @Test
    fun `numbers with zeros prefixes and separators are accepted`() {
        listOf("1234", "01234", "28/1234", "AB-1234", "AB 1234", "123.45", "7", "col-1234", " 1234 ")
            .forEach { assertTrue(LicenseNumberValidator.isValid(it), "'$it' must be accepted") }
        assertTrue(LicenseNumberValidator.isValid("1".repeat(LicenseNumberValidator.MAX_LENGTH)))
    }

    @Test
    fun `empty or separator-only or unsupported characters are rejected`() {
        listOf("", "   ", "---", "./ -", "12_34", "12#34", "Ñ123")
            .forEach { assertFalse(LicenseNumberValidator.isValid(it), "'$it' must be rejected") }
        assertFalse(LicenseNumberValidator.isValid("1".repeat(LicenseNumberValidator.MAX_LENGTH + 1)))
    }

    @Test
    fun `normalization keeps zeros and separators`() {
        assertEquals("AB 1234", LicenseNumberValidator.normalize("  ab   1234 "))
        assertEquals("01234", LicenseNumberValidator.normalize("01234"))
        assertEquals("28/12-3.4", LicenseNumberValidator.normalize("28/12-3.4"))
    }
}
