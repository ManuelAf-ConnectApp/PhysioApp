package com.connectapp.data.resources

import com.connectapp.data.file.SaveFile
import com.connectapp.designresources.TokensResources
import com.connectapp.domain.model.Invoice
import com.connectapp.domain.model.Report
import com.connectapp.domain.repository.PatientRepository
import com.connectapp.domain.repository.ProfessionalRepository
import com.connectapp.domain.resources.GeneratePdfResource
import org.jetbrains.compose.resources.getString

class GeneratePdfResourceImpl(
    private val width: Double,
    private val height: Double,
    private val saveFile: SaveFile,
    private val patientRepository: PatientRepository,
    private val professionalRepository: ProfessionalRepository
) : GeneratePdfResource {

    override suspend fun generateReportPdf(report: Report): Result<Boolean> {
        val pdfHelper = PDFHelper(patientRepository, professionalRepository)

        val pdf =
            pdfHelper.generatePdfReportBuilder(
                report = report, width = width, height = height
            )

        val success = saveFile.saveAndExport(fileName = report.title, bytes = pdf)
        if (success) {
            notifyOrShareFile(fileName = report.title)
        }
        return Result.success(value = success)
    }

    override suspend fun generateInvoicePdf(invoice: Invoice): Result<Boolean> {
        val rawInvoiceTitle = getString(TokensResources.pdfInvoiceTitle)
        val fileName = "${rawInvoiceTitle}_${invoice.invoiceNumber}"

        val pdfHelper = PDFHelper(patientRepository, professionalRepository)

        val pdf =
            pdfHelper.generatePdfInvoiceBuilder(
                invoice = invoice, width = width, height = height
            )

        val success = saveFile.saveAndExport(
            fileName = fileName,
            bytes = pdf
        )
        if (success) {
            notifyOrShareFile(fileName = fileName)
        }
        return Result.success(value = success)
    }

    override fun notifyOrShareFile(fileName: String) {
        saveFile.notifyOrShareFile(fileName = fileName)
    }
}