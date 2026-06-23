package com.connectapp.domain.usecase

import com.connectapp.domain.model.Invoice
import com.connectapp.domain.model.Report
import com.connectapp.domain.resources.GeneratePdfResource

class GenerateInvoicePdfUseCase(
    private val generatePdfResource: GeneratePdfResource
) {
    suspend operator fun invoke(invoice: Invoice): Result<Boolean> {
        return generatePdfResource.generateInvoicePdf(invoice)
    }
}