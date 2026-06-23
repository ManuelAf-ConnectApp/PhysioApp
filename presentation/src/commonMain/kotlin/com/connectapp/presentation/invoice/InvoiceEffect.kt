package com.connectapp.presentation.invoice

sealed interface InvoiceEffect {
    object NavigateBack : InvoiceEffect
    object NavigateToCreatePatient : InvoiceEffect
    data class ShowError(val message: String) : InvoiceEffect
    data class ShowSuccess(val message: String) : InvoiceEffect
}
