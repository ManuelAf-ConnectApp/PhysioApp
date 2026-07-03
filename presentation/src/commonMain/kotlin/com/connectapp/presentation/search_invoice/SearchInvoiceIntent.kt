package com.connectapp.presentation.search_invoice

sealed interface SearchInvoiceIntent {
    data class OnSearchQueryChange(val query: String) : SearchInvoiceIntent
    data class OnSearchTypeChange(val type: SearchType) : SearchInvoiceIntent
    data object OnSearchClick : SearchInvoiceIntent
    data class OnInvoiceSelected(val invoice: com.connectapp.domain.model.Invoice) : SearchInvoiceIntent
    data object OnGeneratePDFClick : SearchInvoiceIntent
}
