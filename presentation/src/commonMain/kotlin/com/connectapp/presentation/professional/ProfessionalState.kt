package com.connectapp.presentation.professional

import com.connectapp.domain.model.Specialty
import com.connectapp.domain.validator.model.ValidationError

data class ProfessionalState(
    val name: String = "",
    val nameError: ValidationError? = null,
    val surname: String = "",
    val surnameError: ValidationError? = null,
    val specialty: String = "",
    val specialtyError: ValidationError? = null,
    val specialties: List<Specialty> = emptyList(),
    val collegiateNumber: String = "",
    val collegiateNumberError: ValidationError? = null,
    val email: String = "",
    val emailError: ValidationError? = null,
    val phone: String = "",
    val phoneError: ValidationError? = null,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null,
    val notes: String = "",
    val notesError: ValidationError? = null
) {
    val isFormValid: Boolean
        get() = name.isNotBlank() && surname.isNotBlank() && specialty.isNotBlank() &&
                collegiateNumber.isNotBlank() && email.isNotBlank() && phone.isNotBlank() &&
                nameError == null && surnameError == null && specialtyError == null &&
                collegiateNumberError == null && emailError == null && phoneError == null
}
