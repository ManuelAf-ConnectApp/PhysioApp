package com.connectapp.presentation.invoices.pdf

import com.connectapp.commonresources.badge_unpaid
import com.connectapp.commonresources.export.ExportedDocument
import com.connectapp.commonresources.export.PdfPageTexts
import com.connectapp.commonresources.format.formatDateTime
import com.connectapp.commonresources.invoice_not_fiscal_explanation
import com.connectapp.commonresources.invoice_not_fiscal_notice
import com.connectapp.commonresources.invoice_paid_with
import com.connectapp.commonresources.invoice_vat_exempt
import com.connectapp.commonresources.invoice_voided_on
import com.connectapp.commonresources.label_amount_plain
import com.connectapp.commonresources.label_concept
import com.connectapp.commonresources.label_paid
import com.connectapp.commonresources.label_patient
import com.connectapp.commonresources.label_payment_status
import com.connectapp.commonresources.pdf_invoice_file_name
import com.connectapp.commonresources.pdf_invoice_heading
import com.connectapp.commonresources.pdf_label_date
import com.connectapp.commonresources.pdf_label_issuer
import com.connectapp.commonresources.pdf_license_number
import com.connectapp.core.pdf.PdfDocumentBuilder
import com.connectapp.domain.invoices.Invoice
import com.connectapp.presentation.invoices.label
import org.jetbrains.compose.resources.getString
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * The receipt as an A4 PDF (D6): number and date, "this document is not an invoice", issuer and
 * patient as issued, concept, amount, collection status and the VAT exemption. A voided receipt
 * says so above everything else.
 */
internal fun buildInvoicePdf(invoice: Invoice, texts: InvoicePdfTexts): ByteArray =
    PdfDocumentBuilder(title = texts.heading, footer = texts.page.footer, pageLabel = texts.page::pageLabel)
        .heading(texts.heading)
        .apply { texts.voidedNotice?.let { notice(it.uppercase()) } }
        .notice(texts.notFiscalNotice)
        .field(texts.dateLabel, texts.date)
        .field(texts.issuerLabel, texts.issuer)
        .field(texts.patientLabel, texts.patient)
        .field(texts.conceptLabel, invoice.concept)
        .field(texts.amountLabel, invoice.amount.format())
        .field(texts.statusLabel, texts.status)
        .rule()
        .note(texts.vatNote)
        .note(texts.notFiscalExplanation)
        .build()

/** Builds the receipt PDF in the app's language, named after its number. */
@OptIn(ExperimentalTime::class)
internal suspend fun exportInvoicePdf(invoice: Invoice): ExportedDocument {
    val issuer = invoice.issuer
    val recipient = invoice.recipient
    val issuerDetails = listOfNotNull(
        "NIF ${issuer.nif}",
        issuer.college?.displayName,
        getString(pdf_license_number, issuer.licenseNumber),
    ).joinToString(" · ")
    val method = invoice.paymentMethod
    val status = when {
        !invoice.paid -> getString(badge_unpaid)
        method != null -> getString(invoice_paid_with, getString(method.label))
        else -> getString(label_paid)
    }
    val texts = InvoicePdfTexts(
        heading = getString(pdf_invoice_heading, invoice.number),
        notFiscalNotice = getString(invoice_not_fiscal_notice),
        voidedNotice = invoice.voiding?.let {
            getString(invoice_voided_on, formatDateTime(it.voidedAtEpochMillis), it.reason)
        },
        dateLabel = getString(pdf_label_date),
        date = formatDateTime(invoice.issuedAtEpochMillis),
        issuerLabel = getString(pdf_label_issuer),
        issuer = "${issuer.firstName} ${issuer.lastName}\n$issuerDetails",
        patientLabel = getString(label_patient),
        patient = "${recipient.firstName} ${recipient.lastName}\n" +
            "${getString(recipient.documentType.label)} ${recipient.documentNumber}",
        conceptLabel = getString(label_concept),
        amountLabel = getString(label_amount_plain),
        statusLabel = getString(label_payment_status),
        status = status,
        vatNote = getString(invoice_vat_exempt),
        notFiscalExplanation = getString(invoice_not_fiscal_explanation),
        page = PdfPageTexts.load(Clock.System.now().toEpochMilliseconds()),
    )
    return ExportedDocument(getString(pdf_invoice_file_name, invoice.number), buildInvoicePdf(invoice, texts))
}
