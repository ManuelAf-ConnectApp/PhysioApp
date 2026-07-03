package com.connectapp.presentation.report

import com.connectapp.domain.model.Patient
import com.connectapp.domain.model.Professional

data class ReportState(
    val professionals: List<Professional>,
    val selectedProfessional: Professional?,
    val patientName: String,
    val suggestedPatients: List<Patient>,
    val selectedPatient: Patient?,
    val reportTitle: String,
    val date: String,
    val clinicalContent: String,
    val diagnosis: String,
    val treatment: String,
    val isLoading: Boolean,
    val isSaved: Boolean,
    val errorMessage: String?
) {
    companion object {
        val EMPTY = ReportState(
            professionals = emptyList(),
            selectedProfessional = null,
            patientName = "",
            suggestedPatients = emptyList(),
            selectedPatient = null,
            reportTitle = "",
            date = "",
            clinicalContent = "",
            diagnosis = "",
            treatment = "",
            isLoading = false,
            isSaved = false,
            errorMessage = null
        )
    }
    val isFormValid: Boolean
        get() = selectedProfessional != null && patientName.isNotBlank() &&
                date.isNotBlank() && clinicalContent.isNotBlank() && diagnosis.isNotBlank() && treatment.isNotBlank()
}
