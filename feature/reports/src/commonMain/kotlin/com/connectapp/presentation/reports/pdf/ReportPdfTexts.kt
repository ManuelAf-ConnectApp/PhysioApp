package com.connectapp.presentation.reports.pdf

import com.connectapp.commonresources.export.PdfPageTexts

/** Every text of a report PDF, already in the app's language and filled in for the report. */
internal data class ReportPdfTexts(
    val heading: String,
    val confidential: String,
    val annulledNotice: String?,
    val dates: String,
    val patientLabel: String,
    val patient: String,
    val professionalLabel: String,
    val reportLabel: String,
    val conditionLabel: String,
    val page: PdfPageTexts,
) {
    override fun toString(): String = "ReportPdfTexts"
}
