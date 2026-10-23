package com.connectapp.presentation.reports.pdf

import com.connectapp.commonresources.document_type_nif_nie
import com.connectapp.commonresources.document_type_other
import com.connectapp.commonresources.document_type_passport
import com.connectapp.commonresources.export.ExportedDocument
import com.connectapp.commonresources.export.PdfPageTexts
import com.connectapp.commonresources.format.formatDateTime
import com.connectapp.commonresources.label_condition_text
import com.connectapp.commonresources.label_patient
import com.connectapp.commonresources.label_professional
import com.connectapp.commonresources.label_report_text
import com.connectapp.commonresources.pdf_report_confidential
import com.connectapp.commonresources.pdf_report_file_name
import com.connectapp.commonresources.pdf_report_heading
import com.connectapp.commonresources.report_annulled_on
import com.connectapp.commonresources.report_created_on
import com.connectapp.commonresources.report_modified_on
import com.connectapp.core.pdf.PdfDocumentBuilder
import com.connectapp.domain.reports.ClinicalReport
import com.connectapp.domain.shared.validation.IdentityDocumentType
import org.jetbrains.compose.resources.getString
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * The current version of a clinical report as an A4 PDF (D17): code, dates, patient, author and
 * both texts, marked confidential. An annulled report says so above everything else; earlier
 * versions are only counted, never printed.
 */
internal fun buildReportPdf(report: ClinicalReport, texts: ReportPdfTexts): ByteArray =
    PdfDocumentBuilder(title = texts.heading, footer = texts.page.footer, pageLabel = texts.page::pageLabel)
        .heading(texts.heading)
        .apply { texts.annulledNotice?.let { notice(it.uppercase()) } }
        .notice(texts.confidential)
        .note(texts.dates)
        .field(texts.patientLabel, texts.patient)
        .field(texts.professionalLabel, "${report.professional.firstName} ${report.professional.lastName}")
        .field(texts.reportLabel, report.reportText)
        .field(texts.conditionLabel, report.conditionText)
        .build()

/** Builds the report PDF in the app's language, named after its code. */
@OptIn(ExperimentalTime::class)
internal suspend fun exportReportPdf(report: ClinicalReport): ExportedDocument {
    val created = getString(report_created_on, formatDateTime(report.createdAtEpochMillis))
    val modified = getString(report_modified_on, formatDateTime(report.updatedAtEpochMillis), report.versionCount)
    val dates = if (report.wasEdited) "$created · $modified" else created
    val documentType = when (report.patient.documentType) {
        IdentityDocumentType.NIF_NIE -> document_type_nif_nie
        IdentityDocumentType.PASSPORT -> document_type_passport
        IdentityDocumentType.OTHER -> document_type_other
    }
    val texts = ReportPdfTexts(
        heading = getString(pdf_report_heading, report.code),
        confidential = getString(pdf_report_confidential),
        annulledNotice = report.annulment?.let {
            getString(report_annulled_on, formatDateTime(it.annulledAtEpochMillis), it.reason)
        },
        dates = dates,
        patientLabel = getString(label_patient),
        patient = "${report.patient.firstName} ${report.patient.lastName}\n" +
            "${getString(documentType)} ${report.patient.documentNumber}",
        professionalLabel = getString(label_professional),
        reportLabel = getString(label_report_text),
        conditionLabel = getString(label_condition_text),
        page = PdfPageTexts.load(Clock.System.now().toEpochMilliseconds()),
    )
    return ExportedDocument(getString(pdf_report_file_name, report.code), buildReportPdf(report, texts))
}
