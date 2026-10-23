package com.connectapp.domain.professionals

import com.connectapp.domain.shared.validation.EmailValidator
import com.connectapp.domain.shared.validation.LicenseNumberValidator
import com.connectapp.domain.shared.validation.NameValidator
import com.connectapp.domain.shared.validation.PhoneValidator
import com.connectapp.domain.shared.validation.SpanishIdValidator

enum class ProfessionalField { NIF, COLLEGE, LICENSE_NUMBER, FIRST_NAME, LAST_NAME, EMAIL, PHONE }

/**
 * D9 (confirmed 2026-10-20): NIF/NIE, college, licence number and names are required; email and
 * phone are optional but must be well formed when given; address and notes are free text.
 */
object ProfessionalValidation {
    fun errors(draft: ProfessionalDraft): Set<ProfessionalField> = buildSet {
        if (!SpanishIdValidator.isValid(draft.nif)) add(ProfessionalField.NIF)
        if (draft.college == null) add(ProfessionalField.COLLEGE)
        if (!LicenseNumberValidator.isValid(draft.licenseNumber)) add(ProfessionalField.LICENSE_NUMBER)
        if (!NameValidator.isValid(draft.firstName)) add(ProfessionalField.FIRST_NAME)
        if (!NameValidator.isValid(draft.lastName)) add(ProfessionalField.LAST_NAME)
        if (draft.email.isNotBlank() && !EmailValidator.isValid(draft.email)) add(ProfessionalField.EMAIL)
        if (draft.phone.isNotBlank() && !PhoneValidator.isValid(draft.phone)) add(ProfessionalField.PHONE)
    }
}
