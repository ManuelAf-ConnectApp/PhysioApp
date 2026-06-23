package com.connectapp.presentation.search_invoice

sealed interface SearchInvoiceEffect {
    data class ShowSnackBar(val message: String) : SearchInvoiceEffect
}