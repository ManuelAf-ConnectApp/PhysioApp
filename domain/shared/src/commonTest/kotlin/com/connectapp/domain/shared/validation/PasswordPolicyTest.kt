package com.connectapp.domain.shared.validation

import com.connectapp.domain.shared.validation.PasswordPolicy.Violation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PasswordPolicyTest {

    @Test
    fun `a password meeting every rule is valid`() {
        assertTrue(PasswordPolicy.isValid("Fisio-Clinica1"))
        assertTrue(PasswordPolicy.isValid("Ñandú corre 2026!"))
        assertEquals(emptySet(), PasswordPolicy.violations("Abcdefghij1!"))
    }

    @Test
    fun `minimum length is twelve characters`() {
        assertEquals(12, PasswordPolicy.MIN_LENGTH)
        assertEquals(setOf(Violation.TOO_SHORT), PasswordPolicy.violations("Abcdefghi1!"))
        assertTrue(PasswordPolicy.isValid("Abcdefghij1!"))
    }

    @Test
    fun `each missing character class is reported`() {
        assertEquals(setOf(Violation.NO_UPPERCASE), PasswordPolicy.violations("fisio-clinica1"))
        assertEquals(setOf(Violation.NO_LOWERCASE), PasswordPolicy.violations("FISIO-CLINICA1"))
        assertEquals(setOf(Violation.NO_DIGIT), PasswordPolicy.violations("Fisio-Clinica!"))
        assertEquals(setOf(Violation.NO_SPECIAL), PasswordPolicy.violations("FisioClinica12"))
    }

    @Test
    fun `spaces are allowed but do not count as special characters`() {
        assertEquals(setOf(Violation.NO_SPECIAL), PasswordPolicy.violations("Fisio Clinica 1"))
    }

    @Test
    fun `an empty password breaks every applicable rule`() {
        assertEquals(
            setOf(Violation.TOO_SHORT, Violation.NO_UPPERCASE, Violation.NO_LOWERCASE, Violation.NO_DIGIT, Violation.NO_SPECIAL),
            PasswordPolicy.violations(""),
        )
    }

    @Test
    fun `passwords longer than the maximum are rejected`() {
        val tooLong = "Aa1!" + "x".repeat(PasswordPolicy.MAX_LENGTH)
        assertTrue(Violation.TOO_LONG in PasswordPolicy.violations(tooLong))
    }

    @Test
    fun `the confirmation must match exactly and satisfy the policy`() {
        assertTrue(PasswordPolicy.isValidConfirmation("Fisio-Clinica1", "Fisio-Clinica1"))
        assertFalse(PasswordPolicy.isValidConfirmation("Fisio-Clinica1", "Fisio-Clinica2"))
        assertFalse(PasswordPolicy.isValidConfirmation("Fisio-Clinica1", "fisio-clinica1"))
        assertFalse(PasswordPolicy.isValidConfirmation("corta", "corta"))
    }
}
