package com.connectapp.presentation.patient

import com.connectapp.domain.validator.model.ValidationError

data class PatientState(
    val name: String = "",
    val nameError: ValidationError? = null,
    val surname: String = "",
    val surnameError: ValidationError? = null,
    val email: String = "",
    val emailError: ValidationError? = null,
    val phone: String = "",
    val phoneError: ValidationError? = null,
    val dni: String = "",
    val dniError: ValidationError? = null,
    val birthDate: String = "",
    val birthDateError: ValidationError? = null,
    val notes: String = "",
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null
) {
    val isFormValid: Boolean
        get() = name.isNotBlank() &&
                surname.isNotBlank() &&
                email.isNotBlank() &&
                phone.isNotBlank() &&
                dni.isNotBlank() &&
                birthDate.isNotBlank()
}
