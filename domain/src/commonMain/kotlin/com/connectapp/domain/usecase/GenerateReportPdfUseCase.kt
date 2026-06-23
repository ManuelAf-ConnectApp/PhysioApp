package com.connectapp.domain.usecase

import com.connectapp.domain.model.Report
import com.connectapp.domain.resources.GeneratePdfResource

class GenerateReportPdfUseCase(
    private val generatePdfResource: GeneratePdfResource
) {
    suspend operator fun invoke(report: Report): Result<Boolean> {
        return generatePdfResource.generateReportPdf(report)
    }
}