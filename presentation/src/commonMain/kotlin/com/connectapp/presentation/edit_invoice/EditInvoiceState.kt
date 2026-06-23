package com.connectapp.presentation.edit_invoice

import com.connectapp.domain.model.Patient
import com.connectapp.domain.model.Professional

data class EditInvoiceState(
    val isLoading: Boolean = false,
    val invoiceNumber: String = "",
    val patientId: String = "",
    val patientName: String = "",
    val suggestedPatients: List<Patient> = emptyList(),
    val professionals: List<Professional> = emptyList(),
    val professionalId: String = "",
    val date: String = "",
    val amount: String = "",
    val concept: String = "",
    val isPaid: Boolean = false,
    val errorMessage: String? = null
)
