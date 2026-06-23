package com.connectapp.presentation.search_invoice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.connectapp.domain.usecase.SearchInvoicesUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SearchInvoiceViewModel(
    private val searchInvoicesUseCase: SearchInvoicesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SearchInvoiceState())
    val state get() = _state.asStateFlow()

    private val _effects = MutableSharedFlow<SearchInvoiceEffect>()
    val effects get() = _effects

    fun onIntent(intent: SearchInvoiceIntent) {
        when (intent) {
            is SearchInvoiceIntent.OnSearchQueryChange -> {
                _state.update { it.copy(searchQuery = intent.query) }
            }
            is SearchInvoiceIntent.OnSearchTypeChange -> {
                _state.update { it.copy(searchType = intent.type) }
            }
            SearchInvoiceIntent.OnSearchClick -> {
                searchInvoices()
            }
        }
    }

    private fun searchInvoices() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val result = when (_state.value.searchType) {
                SearchType.INVOICE_NUMBER -> {
                    searchInvoicesUseCase.byNumber(_state.value.searchQuery).map { invoice ->
                        if (invoice != null) listOf(invoice) else emptyList()
                    }
                }
                SearchType.PROFESSIONAL -> {
                    searchInvoicesUseCase.byProfessional(_state.value.searchQuery)
                }
                SearchType.PATIENT -> {
                    searchInvoicesUseCase.byPatient(_state.value.searchQuery)
                }
            }

            result.onSuccess { invoices ->
                _state.update { it.copy(isLoading = false, invoices = invoices) }
            }.onFailure { error ->
                _state.update { it.copy(isLoading = false, error = error.message) }
            }
        }
    }
}
