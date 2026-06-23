package com.connectapp.presentation.search_report

import com.connectapp.domain.model.Report

sealed interface SearchReportIntent {
    data class QueryChanged(val query: String) : SearchReportIntent
    data class ReportSelected(val report: Report) : SearchReportIntent

    data object GeneratePDFClicked : SearchReportIntent

}
