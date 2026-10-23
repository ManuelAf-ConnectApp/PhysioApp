package com.connectapp.presentation.invoices.pdf

import com.connectapp.commonresources.export.PdfPageTexts

/** Every text of a receipt PDF, already in the app's language and filled in for [Invoice]. */
internal data class InvoicePdfTexts(
    val heading: String,
    val notFiscalNotice: String,
    val voidedNotice: String?,
    val dateLabel: String,
    val date: String,
    val issuerLabel: String,
    val issuer: String,
    val patientLabel: String,
    val patient: String,
    val conceptLabel: String,
    val amountLabel: String,
    val statusLabel: String,
    val status: String,
    val vatNote: String,
    val notFiscalExplanation: String,
    val page: PdfPageTexts,
) {
    override fun toString(): String = "InvoicePdfTexts"
}
