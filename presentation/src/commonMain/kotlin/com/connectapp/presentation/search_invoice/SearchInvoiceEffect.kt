package com.connectapp.presentation.search_invoice

sealed interface SearchInvoiceEffect {
    data class ShowSnackBar(val message: String) : SearchInvoiceEffect
    data class SuccessGeneratePdf(val message: String) : SearchInvoiceEffect
    data class ErrorGeneratePdf(val message: String) : SearchInvoiceEffect
}