package com.connectapp.domain.repository

import com.connectapp.domain.model.Report

interface ReportRepository {
    suspend fun getReportByPatientEmail(email: String): List<Report>

    suspend fun getReportByProfessionalEmail(email: String): Report?

    suspend fun getAllReports(): List<Report>

    suspend fun searchReports(query: String): List<Report>

    suspend fun insertReport(report: Report): Boolean

    suspend fun updateReport(report: Report): Boolean

    suspend fun deleteReport(id: Long)
}