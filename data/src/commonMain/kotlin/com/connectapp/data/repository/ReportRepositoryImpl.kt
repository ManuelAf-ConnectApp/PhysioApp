package com.connectapp.data.repository

import com.connectapp.data.database.AppDatabase
import com.connectapp.data.mapper.toDomain
import com.connectapp.domain.model.Report
import com.connectapp.domain.repository.ReportRepository

class ReportRepositoryImpl(
    appDatabase: AppDatabase
) : ReportRepository {

    private val reportQueries = appDatabase.appDatabaseQueries

    override suspend fun getReportByPatientEmail(email: String): List<Report> {
        return reportQueries.getReportsByPatientEmail(email).executeAsList().map { it.toDomain() }
    }

    override suspend fun getReportByProfessionalEmail(email: String): Report? {
        return reportQueries.getReportsByProfessionalEmail(email).executeAsList().map { it.toDomain() }.firstOrNull()
    }

    override suspend fun getAllReports(): List<Report> {
        return reportQueries.selectAllReports().executeAsList().map { it.toDomain() }
    }

    override suspend fun searchReports(query: String): List<Report> {
        return reportQueries.searchReports(query).executeAsList().map { it.toDomain() }
    }

    override suspend fun insertReport(report: Report): Boolean {
        val insertReport = reportQueries.insertReport(
            patientId = report.codPatient,
            professionalId = report.codProfessional,
            title = report.title,
            date = report.date,
            clinicalContent = report.clinicalContent,
            diagnosis = report.diagnosis,
            treatment = report.treatment,
        )
        return insertReport.value > 0
    }

    override suspend fun updateReport(report: Report): Boolean {
        val updateReport = reportQueries.updateReport(
            patientId = report.codPatient,
            professionalId = report.codProfessional,
            title = report.title,
            date = report.date,
            clinicalContent = report.clinicalContent,
            diagnosis = report.diagnosis,
            treatment = report.treatment,
            id = report.id
        )
        return updateReport.value > 0
    }

    override suspend fun deleteReport(id: Long) {
        reportQueries.deleteReport(id)
    }

}

