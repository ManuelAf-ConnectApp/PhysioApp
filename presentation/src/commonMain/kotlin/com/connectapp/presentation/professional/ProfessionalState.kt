package com.connectapp.presentation.professional

import com.connectapp.domain.model.Specialty
import com.connectapp.domain.validator.model.ValidationError

data class ProfessionalState(
    val name: String,
    val nameError: ValidationError?,
    val surname: String,
    val surnameError: ValidationError?,
    val specialty: String,
    val specialtyError: ValidationError?,
    val specialties: List<Specialty>,
    val collegiateNumber: String,
    val collegiateNumberError: ValidationError?,
    val email: String,
    val emailError: ValidationError?,
    val phone: String,
    val phoneError: ValidationError?,
    val isLoading: Boolean,
    val isSaved: Boolean,
    val errorMessage: String?,
    val notes: String,
    val notesError: ValidationError?
) {
    companion object {
        val EMPTY = ProfessionalState(
            name = "",
            nameError = null,
            surname = "",
            surnameError = null,
            specialty = "",
            specialtyError = null,
            specialties = emptyList(),
            collegiateNumber = "",
            collegiateNumberError = null,
            email = "",
            emailError = null,
            phone = "",
            phoneError = null,
            isLoading = false,
            isSaved = false,
            errorMessage = null,
            notes = "",
            notesError = null

        )
    }
    val isFormValid: Boolean
        get() = name.isNotBlank() && surname.isNotBlank() && specialty.isNotBlank() &&
                collegiateNumber.isNotBlank() && email.isNotBlank() && phone.isNotBlank() &&
                nameError == null && surnameError == null && specialtyError == null &&
                collegiateNumberError == null && emailError == null && phoneError == null
}
