package com.connectapp.domain.shared.validation

import com.connectapp.domain.shared.validation.IdentityDocumentType.NIF_NIE
import com.connectapp.domain.shared.validation.IdentityDocumentType.OTHER
import com.connectapp.domain.shared.validation.IdentityDocumentType.PASSPORT
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IdentityDocumentValidatorTest {

    @Test
    fun `a NIF or NIE needs its control letter`() {
        assertTrue(IdentityDocumentValidator.isValid(NIF_NIE, "12345678z"))
        assertTrue(IdentityDocumentValidator.isValid(NIF_NIE, "X1234567L"))
        assertFalse(IdentityDocumentValidator.isValid(NIF_NIE, "12345678A"))
        assertEquals("12345678Z", IdentityDocumentValidator.normalize(NIF_NIE, " 12345678-z "))
    }

    @Test
    fun `a passport is 3 to 20 letters or digits`() {
        assertTrue(IdentityDocumentValidator.isValid(PASSPORT, "paa123456"))
        assertTrue(IdentityDocumentValidator.isValid(PASSPORT, "C01X 00T47"))
        assertFalse(IdentityDocumentValidator.isValid(PASSPORT, "AB"))
        assertFalse(IdentityDocumentValidator.isValid(PASSPORT, "AB-123"))
        assertFalse(IdentityDocumentValidator.isValid(PASSPORT, "A".repeat(21)))
        assertEquals("PAA123456", IdentityDocumentValidator.normalize(PASSPORT, " paa 123456 "))
    }

    @Test
    fun `another document accepts free text with at least a letter or digit`() {
        assertTrue(IdentityDocumentValidator.isValid(OTHER, "DNI tutor 12345678Z"))
        assertTrue(IdentityDocumentValidator.isValid(OTHER, "7"))
        assertFalse(IdentityDocumentValidator.isValid(OTHER, " - / "))
        assertFalse(IdentityDocumentValidator.isValid(OTHER, ""))
        assertFalse(IdentityDocumentValidator.isValid(OTHER, "A".repeat(IdentityDocumentValidator.OTHER_MAX + 1)))
        assertEquals("DNI TUTOR 1", IdentityDocumentValidator.normalize(OTHER, " dni   tutor 1 "))
    }
}
