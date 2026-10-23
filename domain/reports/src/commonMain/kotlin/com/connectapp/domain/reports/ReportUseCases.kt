package com.connectapp.domain.reports

import com.connectapp.domain.shared.patient.PatientOption
import com.connectapp.domain.usecase.CurrentAuthorUseCase
import kotlinx.coroutines.flow.Flow

/** "Crear informe": the professional is the one of the session, not editable. */
class CreateReportUseCase(private val repository: ReportRepository, private val currentAuthor: CurrentAuthorUseCase) {
    suspend operator fun invoke(patientId: String?, reportText: String, conditionText: String): Result<String> {
        val author = currentAuthor()
        return when {
            ReportValidation.errors(patientId, reportText, conditionText).isNotEmpty() || patientId == null ->
                Result.failure(ReportError.InvalidInput)
            author == null -> Result.failure(ReportError.NotAuthenticated)
            else -> repository.create(author, patientId, reportText.trim(), conditionText.trim())
        }
    }
}

/** D17: only the author edits, and the replaced text is kept as a version. */
class UpdateReportUseCase(private val repository: ReportRepository, private val currentAuthor: CurrentAuthorUseCase) {
    suspend operator fun invoke(id: String, reportText: String, conditionText: String): Result<Unit> {
        val author = currentAuthor()
        return when {
            reportText.isBlank() || conditionText.isBlank() -> Result.failure(ReportError.InvalidInput)
            author == null -> Result.failure(ReportError.NotAuthenticated)
            else -> repository.update(id, author, reportText.trim(), conditionText.trim())
        }
    }
}

/** D17: reports are never deleted; the author may annul one with a reason. */
class AnnulReportUseCase(private val repository: ReportRepository, private val currentAuthor: CurrentAuthorUseCase) {
    suspend operator fun invoke(id: String, reason: String): Result<Unit> {
        val author = currentAuthor()
        return when {
            !ReportValidation.isValidAnnulmentReason(reason) -> Result.failure(ReportError.InvalidInput)
            author == null -> Result.failure(ReportError.NotAuthenticated)
            else -> repository.annul(id, author, reason.trim())
        }
    }
}

class ObserveReportUseCase(private val repository: ReportRepository) {
    operator fun invoke(id: String): Flow<ClinicalReport?> = repository.observe(id)
}

class ObserveReportVersionsUseCase(private val repository: ReportRepository) {
    operator fun invoke(id: String): Flow<List<ReportVersion>> = repository.observeVersions(id)
}

class SearchReportsUseCase(private val repository: ReportRepository) {
    operator fun invoke(criterion: ReportSearchCriterion, query: String): Flow<List<ReportSummary>> =
        repository.search(criterion, query.trim())
}

class SuggestPatientsUseCase(private val repository: ReportRepository) {
    suspend operator fun invoke(query: String): List<PatientOption> =
        if (query.isBlank()) emptyList() else repository.suggestPatients(query.trim())
}
