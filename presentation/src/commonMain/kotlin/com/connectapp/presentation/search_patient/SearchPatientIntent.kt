package com.connectapp.presentation.search_patient

import com.connectapp.domain.model.Patient

sealed interface SearchPatientIntent {
    data class EmailChanged(val email: String) : SearchPatientIntent
    data class PhoneChanged(val phone: String) : SearchPatientIntent
    data class DNIChanged(val dni: String) : SearchPatientIntent
    data object SearchClicked : SearchPatientIntent
    data class PatientSelected(val patient: Patient) : SearchPatientIntent

    data object ClearSearch : SearchPatientIntent
}
