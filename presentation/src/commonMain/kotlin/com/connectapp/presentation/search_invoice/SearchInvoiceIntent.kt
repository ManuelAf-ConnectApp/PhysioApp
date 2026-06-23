package com.connectapp.presentation.search_invoice

sealed interface SearchInvoiceIntent {
    data class OnSearchQueryChange(val query: String) : SearchInvoiceIntent
    data class OnSearchTypeChange(val type: SearchType) : SearchInvoiceIntent
    data object OnSearchClick : SearchInvoiceIntent
}
