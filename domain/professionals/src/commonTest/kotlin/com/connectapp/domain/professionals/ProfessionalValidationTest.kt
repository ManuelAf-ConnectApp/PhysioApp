package com.connectapp.domain.professionals

import com.connectapp.domain.shared.professional.ProfessionalCollege
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal val validDraft = ProfessionalDraft(
    nif = "12345678Z",
    college = ProfessionalCollege.MADRID,
    licenseNumber = "28/1234",
    firstName = "Ana",
    lastName = "Gil",
)

class ProfessionalValidationTest {

    @Test
    fun `required fields only is a valid draft`() {
        assertTrue(ProfessionalValidation.errors(validDraft).isEmpty())
    }

    @Test
    fun `each required field is reported`() {
        assertEquals(
            setOf(
                ProfessionalField.NIF,
                ProfessionalField.COLLEGE,
                ProfessionalField.LICENSE_NUMBER,
                ProfessionalField.FIRST_NAME,
                ProfessionalField.LAST_NAME,
            ),
            ProfessionalValidation.errors(ProfessionalDraft()),
        )
    }

    @Test
    fun `optional email and phone are checked only when given`() {
        assertTrue(ProfessionalValidation.errors(validDraft.copy(email = " ", phone = "")).isEmpty())
        assertEquals(
            setOf(ProfessionalField.EMAIL, ProfessionalField.PHONE),
            ProfessionalValidation.errors(validDraft.copy(email = "ana@", phone = "123")),
        )
        assertTrue(ProfessionalValidation.errors(validDraft.copy(email = "ana@clinica.es", phone = "+34 600 123 456")).isEmpty())
    }
}
