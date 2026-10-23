package com.connectapp.presentation.reports

import com.connectapp.domain.reports.ClinicalReport
import com.connectapp.domain.reports.ReportAnnulment
import com.connectapp.domain.reports.ReportError
import com.connectapp.domain.reports.ReportPatient
import com.connectapp.domain.reports.ReportProfessional
import com.connectapp.domain.reports.ReportRepository
import com.connectapp.domain.reports.ReportSearchCriterion
import com.connectapp.domain.reports.ReportSummary
import com.connectapp.domain.reports.ReportVersion
import com.connectapp.domain.shared.patient.PatientOption
import com.connectapp.domain.shared.validation.IdentityDocumentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

internal val pepe = PatientOption("pat-1", "Pepe", "Ruiz", IdentityDocumentType.NIF_NIE, "X1234567L")

/** In-memory [ReportRepository] with the author rules of D17. */
internal class FakeReportRepository : ReportRepository {
    val reports = MutableStateFlow<Map<String, ClinicalReport>>(emptyMap())
    val versions = mutableMapOf<String, MutableList<ReportVersion>>()
    var lastSearch: Pair<ReportSearchCriterion, String>? = null
        private set

    fun add(id: String, authorId: String, annulled: Boolean = false) {
        reports.value += id to ClinicalReport(
            id = id,
            code = "INF-AAAA2222",
            patient = ReportPatient(pepe.id, pepe.firstName, pepe.lastName, pepe.documentType, pepe.documentNumber),
            professional = ReportProfessional(authorId, "Ana", "Gil"),
            reportText = "Datos",
            conditionText = "Lumbalgia",
            createdAtEpochMillis = 0,
            updatedAtEpochMillis = 0,
            annulment = if (annulled) ReportAnnulment("Error", 1) else null,
            versionCount = 0,
        )
    }

    override suspend fun create(authorId: String, patientId: String, reportText: String, conditionText: String): Result<String> {
        val id = "rep-${reports.value.size + 1}"
        add(id, authorId)
        return Result.success(id)
    }

    override suspend fun update(id: String, authorId: String, reportText: String, conditionText: String): Result<Unit> {
        val current = reports.value[id] ?: return Result.failure(ReportError.NotFound)
        if (current.professional.id != authorId) return Result.failure(ReportError.NotAuthor)
        versions.getOrPut(id) { mutableListOf() } += ReportVersion(current.reportText, current.conditionText, 0, 1)
        reports.value += id to current.copy(reportText = reportText, conditionText = conditionText, versionCount = current.versionCount + 1)
        return Result.success(Unit)
    }

    override suspend fun annul(id: String, authorId: String, reason: String): Result<Unit> {
        val current = reports.value[id] ?: return Result.failure(ReportError.NotFound)
        if (current.professional.id != authorId) return Result.failure(ReportError.NotAuthor)
        reports.value += id to current.copy(annulment = ReportAnnulment(reason, 2))
        return Result.success(Unit)
    }

    override fun observe(id: String): Flow<ClinicalReport?> = reports.map { it[id] }

    override fun observeVersions(id: String): Flow<List<ReportVersion>> = reports.map { versions[id].orEmpty().toList() }

    override fun search(criterion: ReportSearchCriterion, query: String): Flow<List<ReportSummary>> {
        lastSearch = criterion to query
        return reports.map { all ->
            all.values.map { ReportSummary(it.id, it.code, "Pepe Ruiz", "X1234567L", "Ana Gil", 0, it.isAnnulled) }
        }
    }

    override suspend fun suggestPatients(query: String): List<PatientOption> =
        listOf(pepe).filter { it.firstName.contains(query, ignoreCase = true) }
}
