package com.connectapp.presentation.search_patient

import com.connectapp.domain.model.Patient
import com.connectapp.domain.validator.model.ValidationError

data class SearchPatientState(
    val email: String = "",
    val phone: String = "",
    val dni: String = "",
    val emailError: ValidationError? = null,
    val phoneError: ValidationError? = null,
    val dniError: ValidationError? = null,
    val isLoading: Boolean = false,
    val results: List<Patient> = emptyList(),
    val selectedPatient: Patient? = null,
    val error: String? = null
) {

    companion object {
        val DATA_EMPTY = SearchPatientState()
    }

    val isEmailEnabled: Boolean get() = phone.isEmpty() && dni.isEmpty()
    val isPhoneEnabled: Boolean get() = email.isEmpty() && dni.isEmpty()
    val isDNIEnabled: Boolean get() = email.isEmpty() && phone.isEmpty()
    
    val canSearch: Boolean get() = email.isNotEmpty() || phone.isNotEmpty() || dni.isNotEmpty()

    val formattedDNI: String get() = dni.uppercase()
}
