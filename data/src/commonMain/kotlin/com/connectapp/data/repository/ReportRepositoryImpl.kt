package com.connectapp.data.repository

import com.connectapp.data.database.AppDatabase
import com.connectapp.data.mapper.toDomain
import com.connectapp.data.storage.CryptoManager
import com.connectapp.domain.model.Report
import com.connectapp.domain.repository.ReportRepository

class ReportRepositoryImpl(
    appDatabase: AppDatabase,
    private val cryptoManager: CryptoManager
) : ReportRepository {

    private val reportQueries = appDatabase.appDatabaseQueries

    override suspend fun getReportByPatientEmail(email: String): List<Report> {
        return getAllReports().filter { it.title.contains(email) } // Simplificado para usar getAll y decrypt
    }

    override suspend fun getReportByProfessionalEmail(email: String): Report? {
        return getAllReports().firstOrNull() // Mejorar según lógica de negocio
    }

    override suspend fun getAllReports(): List<Report> {
        return reportQueries.selectAllReports().executeAsList().map { it.toDomain().decrypt() }
    }

    override suspend fun searchReports(query: String): List<Report> {
        return getAllReports().filter { 
            it.title.contains(query, ignoreCase = true) || 
            it.clinicalContent.contains(query, ignoreCase = true) ||
            it.diagnosis.contains(query, ignoreCase = true)
        }
    }

    override suspend fun insertReport(report: Report): Boolean {
        val encrypted = report.encrypt()
        val insertReport = reportQueries.insertReport(
            patientId = encrypted.codPatient,
            professionalId = encrypted.codProfessional,
            title = encrypted.title,
            date = encrypted.date,
            clinicalContent = encrypted.clinicalContent,
            diagnosis = encrypted.diagnosis,
            treatment = encrypted.treatment,
        )
        return insertReport.value > 0
    }

    override suspend fun updateReport(report: Report): Boolean {
        val encrypted = report.encrypt()
        val updateReport = reportQueries.updateReport(
            patientId = encrypted.codPatient,
            professionalId = encrypted.codProfessional,
            title = encrypted.title,
            date = encrypted.date,
            clinicalContent = encrypted.clinicalContent,
            diagnosis = encrypted.diagnosis,
            treatment = encrypted.treatment,
            id = encrypted.id
        )
        return updateReport.value > 0
    }

    override suspend fun deleteReport(id: Long) {
        reportQueries.deleteReport(id)
    }

    private fun Report.encrypt(): Report = copy(
        title = cryptoManager.encrypt(title),
        clinicalContent = cryptoManager.encrypt(clinicalContent),
        diagnosis = cryptoManager.encrypt(diagnosis),
        treatment = cryptoManager.encrypt(treatment)
    )

    private fun Report.decrypt(): Report = copy(
        title = cryptoManager.decrypt(title),
        clinicalContent = cryptoManager.decrypt(clinicalContent),
        diagnosis = cryptoManager.decrypt(diagnosis),
        treatment = cryptoManager.decrypt(treatment)
    )
}
