package com.connectapp.presentation.search_invoice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.connectapp.domain.usecase.GenerateInvoicePdfUseCase
import com.connectapp.domain.usecase.SearchInvoicesUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SearchInvoiceViewModel(
    private val searchInvoicesUseCase: SearchInvoicesUseCase,
    private val generateInvoicePdfUseCase: GenerateInvoicePdfUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(SearchInvoiceState.EMPTY)
    val state get() = _state.asStateFlow()

    private val _effects = MutableSharedFlow<SearchInvoiceEffect>()
    val effects get() = _effects.asSharedFlow()

    init {
        _state.update {
            SearchInvoiceState.EMPTY
        }
    }

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

            is SearchInvoiceIntent.OnInvoiceSelected -> {
                _state.update { it.copy(selectedInvoice = intent.invoice) }
            }

            SearchInvoiceIntent.OnGeneratePDFClick -> {
                generatePdf()
            }
        }
    }

    private fun generatePdf() {
        val invoice = _state.value.selectedInvoice ?: return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            generateInvoicePdfUseCase(invoice)
                .onSuccess {
                    _state.update { it.copy(isLoading = false) }
                    _effects.emit(SearchInvoiceEffect.SuccessGeneratePdf("pdf_save_success"))
                }
                .onFailure {
                    _state.update { it.copy(isLoading = false) }
                    _effects.emit(SearchInvoiceEffect.ErrorGeneratePdf("pdf_generate_error"))
                }
        }
    }

    private fun searchInvoices() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, selectedInvoice = null) }
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
