package com.connectapp.presentation.reports

import com.connectapp.commonresources.error_patient_not_available
import com.connectapp.commonresources.error_report_annulled
import com.connectapp.commonresources.error_report_not_author
import com.connectapp.commonresources.error_unexpected
import com.connectapp.commonresources.report_not_found
import com.connectapp.domain.reports.ReportError
import org.jetbrains.compose.resources.StringResource

internal fun Throwable.toMessage(): StringResource = when (this) {
    ReportError.NotFound -> report_not_found
    ReportError.NotAuthor -> error_report_not_author
    ReportError.Annulled -> error_report_annulled
    ReportError.PatientNotAvailable -> error_patient_not_available
    else -> error_unexpected
}
