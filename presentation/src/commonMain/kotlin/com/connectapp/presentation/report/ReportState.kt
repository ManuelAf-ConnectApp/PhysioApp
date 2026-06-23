package com.connectapp.presentation.report

import com.connectapp.domain.model.Patient
import com.connectapp.domain.model.Professional

data class ReportState(
    val professionals: List<Professional> = emptyList(),
    val selectedProfessional: Professional? = null,
    val patientName: String = "",
    val suggestedPatients: List<Patient> = emptyList(),
    val selectedPatient: Patient? = null,
    val reportTitle: String = "",
    val date: String = "",
    val clinicalContent: String = "",
    val diagnosis: String = "",
    val treatment: String = "",
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null
) {
    val isFormValid: Boolean
        get() = selectedProfessional != null && patientName.isNotBlank() &&
                date.isNotBlank() && clinicalContent.isNotBlank() && diagnosis.isNotBlank() && treatment.isNotBlank()
}
