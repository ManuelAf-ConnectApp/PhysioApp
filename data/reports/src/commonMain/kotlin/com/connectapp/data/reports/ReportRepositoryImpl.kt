package com.connectapp.data.reports

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.connectapp.core.crypto.secureRandomBytes
import com.connectapp.core.database.SearchByCode
import com.connectapp.core.database.physio.PhysioDatabase
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
import com.connectapp.domain.shared.security.ReportCode
import com.connectapp.domain.shared.text.SearchText
import com.connectapp.domain.shared.validation.IdentityDocumentType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Clinical reports over the encrypted [PhysioDatabase] (phase 8; D17).
 *
 * - The visible code is random ([ReportCode]); a collision with an existing code draws again.
 * - Edits are only for the author of an active report, and the replaced text is copied to the
 *   version table first, in the same transaction.
 * - Reports are never deleted: the author may annul one with a reason.
 */
class ReportRepositoryImpl(
    private val database: PhysioDatabase,
    private val now: () -> Long,
    private val randomBytes: (Int) -> ByteArray = ::secureRandomBytes,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ReportRepository {

    private val reports = database.clinicalReportQueries
    private val patients = database.patientQueries

    override suspend fun create(
        authorId: String,
        patientId: String,
        reportText: String,
        conditionText: String,
    ): Result<String> = operation {
        val patient = patients.selectPatientById(patientId).executeAsOneOrNull()
        if (patient == null || patient.archived_at != null) throw ReportError.PatientNotAvailable
        val id = randomUuid()
        val timestamp = now()
        reports.insertReport(id, newCode(), authorId, patientId, reportText, conditionText, timestamp, timestamp)
        id
    }

    override suspend fun update(
        id: String,
        authorId: String,
        reportText: String,
        conditionText: String,
    ): Result<Unit> = operation {
        val current = editableReport(id, authorId)
        if (current.report_text == reportText && current.condition_text == conditionText) return@operation
        val timestamp = now()
        reports.insertVersion(
            id = randomUuid(),
            report_id = id,
            report_text = current.report_text,
            condition_text = current.condition_text,
            saved_at = current.updated_at,
            replaced_at = timestamp,
        )
        reports.updateReportContent(reportText, conditionText, timestamp, id)
    }

    override suspend fun annul(id: String, authorId: String, reason: String): Result<Unit> = operation {
        editableReport(id, authorId)
        val timestamp = now()
        reports.annulReport(annulled_at = timestamp, annulment_reason = reason, updated_at = timestamp, id = id)
    }

    override fun observe(id: String): Flow<ClinicalReport?> =
        reports.selectReportDetail(id).asFlow().mapToOneOrNull(dispatcher).map { row ->
            row?.let {
                ClinicalReport(
                    id = it.id,
                    code = it.code,
                    patient = ReportPatient(
                        id = it.patient_id,
                        firstName = it.patient_first_name,
                        lastName = it.patient_last_name,
                        documentType = IdentityDocumentType.valueOf(it.patient_document_type),
                        documentNumber = it.patient_document_number,
                    ),
                    professional = ReportProfessional(
                        id = it.professional_id,
                        firstName = it.professional_first_name,
                        lastName = it.professional_last_name,
                    ),
                    reportText = it.report_text,
                    conditionText = it.condition_text,
                    createdAtEpochMillis = it.created_at,
                    updatedAtEpochMillis = it.updated_at,
                    annulment = it.annulled_at?.let { at -> ReportAnnulment(it.annulment_reason.orEmpty(), at) },
                    versionCount = it.version_count.toInt(),
                )
            }
        }

    override fun observeVersions(id: String): Flow<List<ReportVersion>> =
        reports.selectVersions(id).asFlow().mapToList(dispatcher).map { rows ->
            rows.map { ReportVersion(it.report_text, it.condition_text, it.saved_at, it.replaced_at) }
        }

    override fun search(criterion: ReportSearchCriterion, query: String): Flow<List<ReportSummary>> {
        // The three searches select the same columns; SQLDelight maps them all to SearchByCode.
        val search = when (criterion) {
            ReportSearchCriterion.CODE ->
                reports.searchByCode(contains(ReportCode.normalizeQuery(query)), SEARCH_LIMIT)
            ReportSearchCriterion.PATIENT_NAME ->
                reports.searchByPatientName(contains(SearchText.normalize(query)), SEARCH_LIMIT, ::SearchByCode)
            ReportSearchCriterion.PATIENT_DOCUMENT ->
                reports.searchByPatientDocument(documentPattern(query), SEARCH_LIMIT, ::SearchByCode)
        }
        return search.asFlow().mapToList(dispatcher).map { rows -> rows.map { it.toSummary() } }
    }

    override suspend fun suggestPatients(query: String): List<PatientOption> = withContext(dispatcher) {
        patients.suggestActivePatients(
            name_pattern = contains(SearchText.normalize(query)),
            document_pattern = documentPattern(query),
            limit = SUGGESTION_LIMIT,
        ).executeAsList().map {
            PatientOption(
                id = it.id,
                firstName = it.first_name,
                lastName = it.last_name,
                documentType = IdentityDocumentType.valueOf(it.document_type),
                documentNumber = it.document_number,
            )
        }
    }

    /** The report, when [authorId] wrote it and it is not annulled (D17). */
    private fun editableReport(id: String, authorId: String) =
        (reports.selectReportById(id).executeAsOneOrNull() ?: throw ReportError.NotFound).also {
            if (it.professional_id != authorId) throw ReportError.NotAuthor
            if (it.annulled_at != null) throw ReportError.Annulled
        }

    private fun newCode(): String {
        repeat(MAX_CODE_ATTEMPTS) {
            val code = ReportCode.fromRandomBytes(randomBytes(ReportCode.RANDOM_LENGTH))
            if (reports.selectReportByCode(code).executeAsOneOrNull() == null) return code
        }
        error("No free report code after $MAX_CODE_ATTEMPTS attempts")
    }

    /** Runs [block] in a transaction on [dispatcher]; [ReportError]s become failures. */
    private suspend fun <T> operation(block: () -> T): Result<T> = withContext(dispatcher) {
        try {
            Result.success(database.transactionWithResult { block() })
        } catch (error: CancellationException) {
            throw error
        } catch (error: ReportError) {
            Result.failure(error)
        } catch (@Suppress("TooGenericExceptionCaught") error: Exception) {
            Result.failure(ReportError.Unknown(error))
        }
    }

    private companion object {
        const val SEARCH_LIMIT = 200L
        const val SUGGESTION_LIMIT = 10L
        const val MAX_CODE_ATTEMPTS = 10
    }
}

private fun SearchByCode.toSummary() = ReportSummary(
    id = id,
    code = code,
    patientName = "$patient_first_name $patient_last_name",
    patientDocument = patient_document_number,
    professionalName = "$professional_first_name $professional_last_name",
    createdAtEpochMillis = created_at,
    isAnnulled = annulled_at != null,
)

/** LIKE patterns with `\` escaping the wildcards the user may type (see ClinicalReport.sq). */
private fun escapeLike(value: String) = value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")
private fun contains(value: String) = "%" + escapeLike(value) + "%"

/** Document keys are upper-case without spaces for every document type. */
private fun documentPattern(query: String) = contains(query.filterNot { it.isWhitespace() }.uppercase())

@OptIn(ExperimentalUuidApi::class)
private fun randomUuid(): String = Uuid.random().toString()
