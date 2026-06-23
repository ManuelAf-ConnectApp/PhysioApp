package com.connectapp.presentation.search_report

interface SearchReportEffect {
    data class ShowError(val message: String) : SearchReportEffect
    data class SuccessGeneratePdf(val message: String) : SearchReportEffect
    data class ErrorGeneratePdf(val message: String) : SearchReportEffect
}