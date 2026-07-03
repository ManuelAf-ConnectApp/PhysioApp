package com.connectapp.presentation.search_report

import com.connectapp.domain.model.Report

data class SearchReportState(
    val query: String,
    val isLoading: Boolean,
    val isError: Boolean,
    val errorMessage: String?,
    val reports: List<Report>,
    val selectedReport: Report?
) {
    companion object {
        val EMPTY = SearchReportState(
            query = "",
            isLoading = false,
            isError = false,
            errorMessage = null,
            reports = emptyList(),
            selectedReport = null
        )
    }
}
