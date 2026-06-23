package com.connectapp.presentation.search_report

import com.connectapp.domain.model.Report

data class SearchReportState(
    val query: String = "",
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String? = null,
    val reports: List<Report> = emptyList(),
    val selectedReport: Report? = null
)
