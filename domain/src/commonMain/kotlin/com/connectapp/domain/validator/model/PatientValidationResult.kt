package com.connectapp.domain.validator.model

data class PatientValidationResult(
    val nameError: ValidationError? = null,
    val surnameError: ValidationError? = null,
    val emailError: ValidationError? = null,
    val phoneError: ValidationError? = null,
    val dniError: ValidationError? = null,
    val birthDateError: ValidationError? = null
) {
    val isValid: Boolean = nameError == null &&
            surnameError == null &&
            emailError == null &&
            phoneError == null &&
            dniError == null &&
            birthDateError == null
}
