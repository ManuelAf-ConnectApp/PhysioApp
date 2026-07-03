package com.connectapp.presentation.search_invoice

import com.connectapp.domain.model.Invoice

data class SearchInvoiceState(
    val isLoading: Boolean,
    val searchQuery: String,
    val searchType: SearchType,
    val invoices: List<Invoice>,
    val selectedInvoice: Invoice? = null,
    val error: String?
) {
    companion object {
        val EMPTY = SearchInvoiceState(
            isLoading = false,
            searchQuery = "",
            searchType = SearchType.INVOICE_NUMBER,
            invoices = emptyList(),
            selectedInvoice = null,
            error = null
        )
    }
}

enum class SearchType {
    INVOICE_NUMBER,
    PROFESSIONAL,
    PATIENT
}
