package com.connectapp.presentation.invoice

import com.connectapp.domain.model.Patient

sealed interface InvoiceIntent {
    data class PatientNameChanged(val name: String) : InvoiceIntent
    data class PatientSelected(val patient: Patient) : InvoiceIntent
    data class InvoiceNumberChanged(val number: String) : InvoiceIntent
    data class DateChanged(val date: String) : InvoiceIntent
    data class AmountChanged(val amount: String) : InvoiceIntent
    data class ConceptChanged(val concept: String) : InvoiceIntent
    data class PaidStatusChanged(val isPaid: Boolean) : InvoiceIntent
    data class ProfessionalSelected(val professionalId: String) : InvoiceIntent
    object SaveClicked : InvoiceIntent
    object AddPatientClicked : InvoiceIntent
    object BackClicked : InvoiceIntent
}
