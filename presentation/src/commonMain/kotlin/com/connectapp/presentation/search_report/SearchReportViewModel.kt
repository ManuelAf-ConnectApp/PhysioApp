package com.connectapp.presentation.search_report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.connectapp.domain.usecase.GenerateReportPdfUseCase
import com.connectapp.domain.usecase.SearchReportsUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class SearchReportViewModel(
    private val searchReportsUseCase: SearchReportsUseCase,
    private val generateReportPdfUseCase: GenerateReportPdfUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SearchReportState.EMPTY)
    val state get() = _state.asStateFlow()

    private val _effect = MutableSharedFlow<SearchReportEffect>()
    val effect get() = _effect.asSharedFlow()

    private val _intents = MutableSharedFlow<SearchReportIntent>()
    val intents get() = _intents.asSharedFlow()


    private var searchJob: Job? = null

    init {
        _state.update {
            SearchReportState.EMPTY
        }
    }

    fun onIntent(intent: SearchReportIntent) {
        reduce(intent)
    }

    private fun reduce(intent: SearchReportIntent) {
        when (intent) {
            is SearchReportIntent.QueryChanged -> {
                onQueryChanged(intent)
            }

            is SearchReportIntent.ReportSelected -> {
                onReportSelected(intent)
            }

            SearchReportIntent.GeneratePDFClicked -> {
                generatePdf()
            }
        }
    }

    private fun onQueryChanged(intent: SearchReportIntent.QueryChanged) {
        _state.update { it.copy(query = intent.query) }
        searchReports(intent.query)
    }

    private fun onReportSelected(intent: SearchReportIntent.ReportSelected) {
        _state.update { it.copy(selectedReport = intent.report) }
    }

    private fun searchReports(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            if (query.isNotEmpty()) {
                delay(duration = 300.milliseconds)
                _state.update { it.copy(isLoading = true) }
                val reports = searchReportsUseCase(query)
                _state.update {
                    it.copy(
                        isLoading = false,
                        reports = reports
                    )
                }
            } else {
                _state.update { it.copy(isLoading = false, reports = emptyList()) }
            }

        }
    }

    private fun generatePdf() {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            _state.value.selectedReport?.let { report ->
                generateReportPdfUseCase(report).onSuccess {
                    if (it) {
                        _effect.emit(SearchReportEffect.SuccessGeneratePdf(message = "pdf_save_success"))
                    } else {
                        _effect.emit(SearchReportEffect.ErrorGeneratePdf(message = "pdf_generate_error"))
                    }
                }


            }
            _state.update { it.copy(isLoading = false) }
        }
    }
}
