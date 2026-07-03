package com.connectapp.presentation.patient

import com.connectapp.domain.validator.model.ValidationError

data class PatientState(
    val name: String,
    val nameError: ValidationError?,
    val surname: String,
    val surnameError: ValidationError?,
    val email: String,
    val emailError: ValidationError?,
    val phone: String,
    val phoneError: ValidationError?,
    val dni: String,
    val dniError: ValidationError?,
    val birthDate: String,
    val birthDateError: ValidationError?,
    val notes: String,
    val isLoading: Boolean,
    val isSaved: Boolean,
    val errorMessage: String?
) {

    companion object {
        val EMPTY = PatientState(
            name = "",
            nameError = null,
            surname = "",
            surnameError = null,
            email = "",
            emailError = null,
            phone = "",
            phoneError = null,
            dni = "",
            dniError = null,
            birthDate = "",
            birthDateError = null,
            notes = "",
            isLoading = false,
            isSaved = false,
            errorMessage = null

        )
    }
    val isFormValid: Boolean
        get() = name.isNotBlank() &&
                surname.isNotBlank() &&
                email.isNotBlank() &&
                phone.isNotBlank() &&
                dni.isNotBlank() &&
                birthDate.isNotBlank()
}
