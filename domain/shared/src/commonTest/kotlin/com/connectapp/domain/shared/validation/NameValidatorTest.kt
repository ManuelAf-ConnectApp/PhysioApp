package com.connectapp.domain.shared.validation

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NameValidatorTest {

    @Test
    fun `any non-blank name is valid including short ones`() {
        assertTrue(NameValidator.isValid("Aina"))
        assertTrue(NameValidator.isValid("  García López  "))
        assertTrue(NameValidator.isValid("Íñigo"))
        assertTrue(NameValidator.isValid("   Ana   "))
        assertTrue(NameValidator.isValid("Li"))
        assertTrue(NameValidator.isValid("O"))
    }

    @Test
    fun `blank names are invalid`() {
        assertFalse(NameValidator.isValid(""))
        assertFalse(NameValidator.isValid("      "))
        assertFalse(NameValidator.isValid("\t\n"))
    }
}
