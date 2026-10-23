package com.connectapp.domain.validation

import com.connectapp.domain.shared.professional.ProfessionalCollege
import com.connectapp.domain.shared.security.RecoveryCode
import com.connectapp.domain.shared.validation.EmailValidator
import com.connectapp.domain.shared.validation.LicenseNumberValidator
import com.connectapp.domain.shared.validation.NameValidator
import com.connectapp.domain.shared.validation.PasswordPolicy
import com.connectapp.domain.shared.validation.SpanishIdValidator

/** Fields of the authentication forms that can carry a validation error. */
enum class AuthField {
    FIRST_NAME,
    LAST_NAME,
    EMAIL,
    CURRENT_PASSWORD,
    PASSWORD,
    PASSWORD_CONFIRMATION,
    RECOVERY_CODE,
    NIF,
    COLLEGE,
    LICENSE_NUMBER,
}

/**
 * Validation rules of the authentication forms, shared by the use cases
 * (which reject invalid input) and the screens (which show the error next to each field).
 */
object AuthValidation {

    fun registrationErrors(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        confirmation: String,
    ): Set<AuthField> = buildSet {
        if (!NameValidator.isValid(firstName)) add(AuthField.FIRST_NAME)
        if (!NameValidator.isValid(lastName)) add(AuthField.LAST_NAME)
        if (!EmailValidator.isValid(email)) add(AuthField.EMAIL)
        addAll(newPasswordErrors(password, confirmation))
    }

    fun passwordResetErrors(
        email: String,
        recoveryCode: String,
        password: String,
        confirmation: String,
    ): Set<AuthField> = buildSet {
        if (!EmailValidator.isValid(email)) add(AuthField.EMAIL)
        if (!RecoveryCode.isWellFormed(recoveryCode)) add(AuthField.RECOVERY_CODE)
        addAll(newPasswordErrors(password, confirmation))
    }

    /** Settings → change password (D15): the current one only needs to be typed; it is verified later. */
    fun passwordChangeErrors(
        currentPassword: String,
        password: String,
        confirmation: String,
    ): Set<AuthField> = buildSet {
        if (currentPassword.isEmpty()) add(AuthField.CURRENT_PASSWORD)
        addAll(newPasswordErrors(password, confirmation))
    }

    fun professionalProfileErrors(
        nif: String,
        college: ProfessionalCollege?,
        licenseNumber: String,
        firstName: String,
        lastName: String,
    ): Set<AuthField> = buildSet {
        if (!SpanishIdValidator.isValid(nif)) add(AuthField.NIF)
        if (college == null) add(AuthField.COLLEGE)
        if (!LicenseNumberValidator.isValid(licenseNumber)) add(AuthField.LICENSE_NUMBER)
        if (!NameValidator.isValid(firstName)) add(AuthField.FIRST_NAME)
        if (!NameValidator.isValid(lastName)) add(AuthField.LAST_NAME)
    }

    private fun newPasswordErrors(password: String, confirmation: String): Set<AuthField> = buildSet {
        if (!PasswordPolicy.isValid(password)) add(AuthField.PASSWORD)
        if (!PasswordPolicy.isValidConfirmation(password, confirmation)) add(AuthField.PASSWORD_CONFIRMATION)
    }
}
