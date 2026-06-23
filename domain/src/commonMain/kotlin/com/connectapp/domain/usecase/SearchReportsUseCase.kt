package com.connectapp.domain.usecase

import com.connectapp.domain.model.Report
import com.connectapp.domain.repository.ReportRepository

class SearchReportsUseCase(
    private val reportRepository: ReportRepository
) {
    suspend operator fun invoke(query: String): List<Report> {
        return if (query.isBlank()) {
            reportRepository.getAllReports()
        } else {
            reportRepository.searchReports(query)
        }
    }
}
