package com.connectapp.presentation.edit_invoice

import com.connectapp.domain.model.Patient

sealed interface EditInvoiceIntent {
    data class LoadInvoice(val invoiceId: String) : EditInvoiceIntent
    data class PatientNameChanged(val name: String) : EditInvoiceIntent
    data class PatientSelected(val patient: Patient) : EditInvoiceIntent
    data class DateChanged(val date: String) : EditInvoiceIntent
    data class ConceptChanged(val concept: String) : EditInvoiceIntent
    data class AmountChanged(val amount: String) : EditInvoiceIntent
    data class PaidStatusChanged(val isPaid: Boolean) : EditInvoiceIntent
    data class ProfessionalSelected(val professionalId: String) : EditInvoiceIntent
    data object SaveClicked : EditInvoiceIntent
    data object BackClicked : EditInvoiceIntent
}
