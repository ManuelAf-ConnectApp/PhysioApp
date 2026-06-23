package com.connectapp.domain.usecase

import com.connectapp.domain.model.Report
import com.connectapp.domain.repository.ReportRepository

class SaveReportUseCase(
    private val reportRepository: ReportRepository
) {
    suspend operator fun invoke(report: Report): Result<Boolean> {
        return Result.success(reportRepository.insertReport(report))
    }
}