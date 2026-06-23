package com.connectapp.presentation.invoice

import com.connectapp.domain.model.Patient
import com.connectapp.domain.model.Professional

data class InvoiceState(
    val patientName: String = "",
    val suggestedPatients: List<Patient> = emptyList(),
    val invoiceNumber: String = "",
    val patientId: String = "",
    val selectedPatient: Patient? = null,
    val professionals: List<Professional> = emptyList(),
    val professionalId: String = "",
    val date: String = "",
    val amount: String = "",
    val concept: String = "",
    val isPaid: Boolean = false,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null
) {
    val isFormValid: Boolean
        get() = patientId.isNotBlank() && professionalId.isNotBlank() && date.isNotBlank() && amount.isNotBlank() && concept.isNotBlank()
}
