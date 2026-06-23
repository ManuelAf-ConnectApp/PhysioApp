package com.connectapp.domain.validator.model

data class ProfessionalValidationResult(
    val nameError: ValidationError? = null,
    val surnameError: ValidationError? = null,
    val emailError: ValidationError? = null,
    val phoneError: ValidationError? = null,
    val specialtyError: ValidationError? = null,
    val collegiateNumberError: ValidationError? = null
) {
    val isValid: Boolean = nameError == null &&
            surnameError == null &&
            emailError == null &&
            phoneError == null &&
            specialtyError == null &&
            collegiateNumberError == null
}
