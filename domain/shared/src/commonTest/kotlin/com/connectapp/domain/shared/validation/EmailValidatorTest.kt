package com.connectapp.domain.shared.validation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EmailValidatorTest {

    @Test
    fun `accepts common addresses without restricting the top-level domain`() {
        listOf(
            "ana@clinica.es",
            "ana.garcia@clinica.com",
            "fisio+agenda@mi-clinica.org",
            "info@fisio.health",
            "a@sub.dominio.cat",
            "contacto@clinica.co.uk",
            "  ana@clinica.es  ",
            "user@xn--fisioterapia-ebb.es",
            "test@example.xn--p1ai",
        ).forEach { assertTrue(EmailValidator.isValid(it), "must accept '$it'") }
    }

    @Test
    fun `rejects malformed addresses`() {
        listOf(
            "",
            "ana",
            "ana@",
            "@clinica.es",
            "ana@clinica",
            "ana@@clinica.es",
            "ana@clin@ica.es",
            "ana@clinica.e",
            "ana@clinica.123",
            ".ana@clinica.es",
            "ana.@clinica.es",
            "an..a@clinica.es",
            "ana@-clinica.es",
            "ana@clinica-.es",
            "ana@clinica..es",
            "ana garcia@clinica.es",
            "añá@clinica.es",
            "a".repeat(65) + "@clinica.es",
            "ana@" + "a".repeat(64) + ".es",
        ).forEach { assertFalse(EmailValidator.isValid(it), "must reject '$it'") }
    }

    @Test
    fun `rejects addresses longer than 254 characters`() {
        // Every label is valid on its own (<= 63 chars); only the total length is over the limit.
        val atLimit = "ana@" + List(4) { "a".repeat(61) }.joinToString(".") + ".es"
        val overLimit = "anaa@" + List(4) { "a".repeat(61) }.joinToString(".") + ".es"
        assertEquals(254, atLimit.length)
        assertTrue(EmailValidator.isValid(atLimit))
        assertFalse(EmailValidator.isValid(overLimit))
    }

    @Test
    fun `normalize trims and lower-cases so duplicates can be detected`() {
        assertEquals("ana@clinica.es", EmailValidator.normalize("  Ana@Clinica.ES "))
        assertEquals(EmailValidator.normalize("ana@clinica.es"), EmailValidator.normalize("ANA@CLINICA.ES"))
    }
}
