package com.connectapp.presentation.search_invoice

import com.connectapp.domain.model.Invoice

data class SearchInvoiceState(
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val searchType: SearchType = SearchType.INVOICE_NUMBER,
    val invoices: List<Invoice> = emptyList(),
    val error: String? = null
)

enum class SearchType {
    INVOICE_NUMBER,
    PROFESSIONAL,
    PATIENT
}
