package com.connectapp.domain.reports

import com.connectapp.domain.shared.patient.PatientOption
import kotlinx.coroutines.flow.Flow

/**
 * Clinical reports in the local database (D17). The author is always passed in
 * by the use cases from the session; the repository checks it on edit and annulment. Failures are
 * always [ReportError]s.
 */
interface ReportRepository {
    /** Creates the report with a new unique visible code; returns its id. */
    suspend fun create(authorId: String, patientId: String, reportText: String, conditionText: String): Result<String>

    /** Keeps the replaced text as a version, then updates it. Only for the author of an active report. */
    suspend fun update(id: String, authorId: String, reportText: String, conditionText: String): Result<Unit>

    /** Only for the author of an active report. */
    suspend fun annul(id: String, authorId: String, reason: String): Result<Unit>

    fun observe(id: String): Flow<ClinicalReport?>

    /** Earlier versions, newest first. */
    fun observeVersions(id: String): Flow<List<ReportVersion>>

    fun search(criterion: ReportSearchCriterion, query: String): Flow<List<ReportSummary>>

    /** Active patients whose name or document matches [query], for the patient picker. */
    suspend fun suggestPatients(query: String): List<PatientOption>
}
