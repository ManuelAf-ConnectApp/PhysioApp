package com.connectapp.presentation.report

sealed interface ReportEffect {
    object NavigateBack : ReportEffect
    object NavigateToCreatePatient : ReportEffect
    data class ShowError(val message: String) : ReportEffect
    data class ShowMessage(val message: String) : ReportEffect
}
