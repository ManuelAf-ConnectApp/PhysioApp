package com.connectapp.presentation.edit_invoice

sealed interface EditInvoiceEffect {
    data object NavigateBack : EditInvoiceEffect
    data class ShowError(val message: String) : EditInvoiceEffect
}
