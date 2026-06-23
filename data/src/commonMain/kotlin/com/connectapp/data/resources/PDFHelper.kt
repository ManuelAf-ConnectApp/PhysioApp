package com.connectapp.data.resources

import com.connectapp.designresources.DimensResources.COLOR_ERROR_B
import com.connectapp.designresources.DimensResources.COLOR_ERROR_G
import com.connectapp.designresources.DimensResources.COLOR_ERROR_R
import com.connectapp.designresources.DimensResources.COLOR_GRAY_DARK
import com.connectapp.designresources.DimensResources.COLOR_PRIMARY_B
import com.connectapp.designresources.DimensResources.COLOR_PRIMARY_G
import com.connectapp.designresources.DimensResources.COLOR_PRIMARY_R
import com.connectapp.designresources.DimensResources.COLOR_SUCCESS_B
import com.connectapp.designresources.DimensResources.COLOR_SUCCESS_G
import com.connectapp.designresources.DimensResources.COLOR_SUCCESS_R
import com.connectapp.designresources.DimensResources.HEADER_HEIGHT
import com.connectapp.designresources.DimensResources.LINE_HEIGHT_MEDIUM
import com.connectapp.designresources.DimensResources.MARGIN_X
import com.connectapp.designresources.DimensResources.OFFSET_X_AMOUNT
import com.connectapp.designresources.DimensResources.OFFSET_X_LABEL
import com.connectapp.designresources.DimensResources.OFFSET_X_STATUS
import com.connectapp.designresources.DimensResources.OFFSET_X_VALUE
import com.connectapp.designresources.DimensResources.OFFSET_X_VALUE_LARGE
import com.connectapp.designresources.DimensResources.OFFSET_Y_HEADER
import com.connectapp.designresources.TokensResources
import com.connectapp.designresources.drawFooter
import com.connectapp.designresources.drawHeader
import com.connectapp.designresources.drawSection
import com.connectapp.domain.model.Invoice
import com.connectapp.domain.model.Report
import com.connectapp.domain.repository.PatientRepository
import com.connectapp.domain.repository.ProfessionalRepository
import io.github.yuroyami.kitepdf.writer.PdfBuilder
import io.github.yuroyami.kitepdf.writer.StandardFont
import org.jetbrains.compose.resources.getString

class PDFHelper(
    private val patientRepository: PatientRepository,
    private val professionalRepository: ProfessionalRepository
) {

    suspend fun generatePdfReportBuilder(report: Report, width: Double, height: Double): ByteArray {

        val dateLabel = getString(TokensResources.pdfDateLabel)
        val patientLabel = getString(TokensResources.pdfPatientLabel)
        val professionalLabel = getString(TokensResources.pdfProfessionalLabel)
        val diagnosisLabel = getString(TokensResources.pdfDiagnosisLabel)
        val clinicalContentLabel =
            getString(TokensResources.pdfClinicalContentLabel)
        val treatmentLabel = getString(TokensResources.pdfTreatmentLabel)
        val headerTitle = getString(TokensResources.pdfReportHeader)
        val footerText = getString(TokensResources.pdfReportFooter)

        val patient = patientRepository.getPatientById(report.codPatient)
        val professional = professionalRepository.getProfessionalById(report.codProfessional)

        val patientFullName = "${patient.firstName} ${patient.lastName}"
        val professionalFullName = "${professional.firstName} ${professional.lastName}"

        return PdfBuilder()
            .setInfo(title = report.title)
            .page(width = width, height = height) {

                // Draw Header
                drawHeader(width = width, height = height, title = headerTitle)

                var currentY = height - HEADER_HEIGHT - OFFSET_Y_HEADER


                // Report Title
                setFillRgb(COLOR_PRIMARY_R, COLOR_PRIMARY_G, COLOR_PRIMARY_B)
                text(
                    StandardFont.HelveticaBold,
                    24.0,
                    MARGIN_X,
                    currentY,
                    report.title.uppercase()
                )

                currentY -= OFFSET_Y_HEADER

                // Date and IDs row
                setFillGray(COLOR_GRAY_DARK)
                text(StandardFont.HelveticaBold, 10.0, MARGIN_X, currentY, dateLabel)
                setFillGray(0.0)
                text(
                    StandardFont.Helvetica,
                    10.0,
                    MARGIN_X + OFFSET_X_LABEL,
                    currentY,
                    report.date
                )

                setFillGray(COLOR_GRAY_DARK)
                text(StandardFont.HelveticaBold, 10.0, width / 2, currentY, patientLabel)
                setFillGray(0.0)
                text(
                    StandardFont.Helvetica,
                    10.0,
                    width / 2 + OFFSET_X_VALUE,
                    currentY,
                    patientFullName
                )

                currentY -= LINE_HEIGHT_MEDIUM

                setFillGray(COLOR_GRAY_DARK)
                text(StandardFont.HelveticaBold, 10.0, width / 2, currentY, professionalLabel)
                setFillGray(0.0)
                text(
                    StandardFont.Helvetica,
                    10.0,
                    width / 2 + OFFSET_X_VALUE_LARGE,
                    currentY,
                    professionalFullName
                )

                currentY -= OFFSET_Y_HEADER

                // Content Sections
                currentY = drawSection(
                    width = width,
                    title = diagnosisLabel,
                    content = report.diagnosis,
                    startY = currentY
                )

                currentY -= 20.0
                currentY = drawSection(
                    width = width,
                    title = clinicalContentLabel,
                    content = report.clinicalContent,
                    startY = currentY
                )

                currentY -= 20.0
                drawSection(
                    width = width,
                    title = treatmentLabel,
                    content = report.treatment,
                    startY = currentY
                )

                // Footer
                drawFooter(text = footerText)
            }
            .build()
    }

    suspend fun generatePdfInvoiceBuilder(
        invoice: Invoice,
        width: Double,
        height: Double
    ): ByteArray {
        val rawInvoiceTitle = getString(TokensResources.pdfInvoiceTitle)
        val dateLabel = getString(TokensResources.pdfDateLabel)
        val patientIdLabel = getString(TokensResources.pdfPatientIdLabel)
        val invoiceNumberLabel = getString(TokensResources.pdfInvoiceNumberLabel)
        val professionalIdLabel =
            getString(TokensResources.pdfProfessionalIdLabel)
        val conceptLabel = getString(TokensResources.pdfConceptLabel)
        val totalAmountLabel = getString(TokensResources.pdfTotalAmountLabel)
        val statusLabel = getString(TokensResources.pdfStatusLabel)
        val paidLabel = getString(TokensResources.pdfPaidLabel)
        val pendingPaymentLabel =
            getString(TokensResources.pdfPendingPaymentLabel)
        val headerTitle = getString(TokensResources.pdfInvoiceHeader)
        val footerText = getString(TokensResources.pdfInvoiceFooter)

        return PdfBuilder()
            .setInfo(title = "$rawInvoiceTitle ${invoice.invoiceNumber}")
            .page(width = width, height = height) {

                // Draw Header
                drawHeader(width = width, height = height, title = headerTitle)

                var currentY = height - HEADER_HEIGHT - OFFSET_Y_HEADER

                // Invoice Title
                setFillRgb(COLOR_PRIMARY_R, COLOR_PRIMARY_G, COLOR_PRIMARY_B)
                text(StandardFont.HelveticaBold, 24.0, MARGIN_X, currentY, rawInvoiceTitle)
                currentY -= OFFSET_Y_HEADER

                // Date and IDs row
                setFillGray(COLOR_GRAY_DARK)
                text(StandardFont.HelveticaBold, 10.0, MARGIN_X, currentY, dateLabel)
                setFillGray(0.0)
                text(
                    StandardFont.Helvetica,
                    10.0,
                    MARGIN_X + OFFSET_X_LABEL,
                    currentY,
                    invoice.date
                )

                setFillGray(COLOR_GRAY_DARK)
                text(StandardFont.HelveticaBold, 10.0, width / 2, currentY, patientIdLabel)
                setFillGray(0.0)
                text(
                    StandardFont.Helvetica,
                    10.0,
                    width / 2 + OFFSET_X_VALUE,
                    currentY,
                    invoice.patientId
                )

                currentY -= LINE_HEIGHT_MEDIUM

                setFillGray(COLOR_GRAY_DARK)
                text(StandardFont.HelveticaBold, 10.0, width / 2, currentY, invoiceNumberLabel)
                setFillGray(0.0)
                text(
                    StandardFont.Helvetica,
                    10.0,
                    width / 2 + OFFSET_X_VALUE,
                    currentY,
                    invoice.invoiceNumber
                )

                currentY -= LINE_HEIGHT_MEDIUM

                setFillGray(COLOR_GRAY_DARK)
                text(
                    StandardFont.HelveticaBold,
                    size = 10.0,
                    x = width / 2,
                    y = currentY,
                    professionalIdLabel
                )
                setFillGray(0.0)
                text(
                    StandardFont.Helvetica,
                    10.0,
                    width / 2 + OFFSET_X_VALUE_LARGE,
                    currentY,
                    invoice.professionalId
                )

                currentY -= OFFSET_Y_HEADER

                // Content Sections
                currentY = drawSection(
                    width = width,
                    title = conceptLabel,
                    content = invoice.concept,
                    startY = currentY
                )

                currentY -= OFFSET_Y_HEADER

                // Amount and Status
                setFillGray(COLOR_GRAY_DARK)
                text(StandardFont.HelveticaBold, 14.0, MARGIN_X, currentY, totalAmountLabel)
                setFillRgb(COLOR_PRIMARY_R, COLOR_PRIMARY_G, COLOR_PRIMARY_B)
                text(
                    StandardFont.HelveticaBold,
                    14.0,
                    MARGIN_X + OFFSET_X_AMOUNT,
                    currentY,
                    "${invoice.amount} €"
                )

                currentY -= 25.0

                setFillGray(COLOR_GRAY_DARK)
                text(StandardFont.HelveticaBold, 12.0, MARGIN_X, currentY, statusLabel)
                val statusText = if (invoice.isPaid) paidLabel else pendingPaymentLabel
                if (invoice.isPaid) {
                    setFillRgb(COLOR_SUCCESS_R, COLOR_SUCCESS_G, COLOR_SUCCESS_B)
                } else {
                    setFillRgb(COLOR_ERROR_R, COLOR_ERROR_G, COLOR_ERROR_B)
                }
                text(
                    StandardFont.HelveticaBold,
                    12.0,
                    MARGIN_X + OFFSET_X_STATUS,
                    currentY,
                    statusText
                )

                // Footer
                drawFooter(text = footerText)
            }
            .build()
    }
}
