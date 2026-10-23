package com.connectapp.data.patients

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.connectapp.core.database.physio.PhysioDatabase
import com.connectapp.domain.patients.ConsentSignature
import com.connectapp.domain.patients.Patient
import com.connectapp.domain.patients.PatientDraft
import com.connectapp.domain.patients.PatientError
import com.connectapp.domain.patients.PatientRepository
import com.connectapp.domain.patients.PatientSearchCriterion
import com.connectapp.domain.patients.PatientSummary
import com.connectapp.domain.patients.SignatureInfo
import com.connectapp.domain.patients.SignatureSigner
import com.connectapp.domain.shared.text.SearchText
import com.connectapp.domain.shared.validation.EmailValidator
import com.connectapp.domain.shared.validation.IdentityDocumentType
import com.connectapp.domain.shared.validation.IdentityDocumentValidator
import com.connectapp.domain.shared.validation.PhoneValidator
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import com.connectapp.core.database.Patient as PatientRow

/**
 * Patient records and signed consents over the encrypted [PhysioDatabase] (phase 7).
 *
 * - Uniqueness (D9): document type + normalized number, reported as
 *   [PatientError.DocumentAlreadyRegistered].
 * - Consent (D13): a patient is never stored without its first signature, and later signatures
 *   are added, never replacing the earlier ones.
 * - Deletion (D8): archiving is the normal "delete"; physical deletion only without reports or
 *   invoices, and it takes the signatures with it.
 */
class PatientRepositoryImpl(
    private val database: PhysioDatabase,
    private val now: () -> Long,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : PatientRepository {

    private val patients = database.patientQueries
    private val signatures = database.patientSignatureQueries

    override suspend fun create(draft: PatientDraft, signature: ConsentSignature): Result<String> = operation {
        val fields = draft.normalized()
        ensureUnique(fields, ownId = null)
        val id = randomUuid()
        val timestamp = now()
        writeOrReportDuplicate(fields, ownId = id) {
            patients.insertPatient(
                id = id,
                document_type = fields.documentType.name,
                document_number = fields.documentNumber,
                document_key = fields.documentKey,
                first_name = fields.firstName,
                last_name = fields.lastName,
                full_name_search = SearchText.fullName(fields.firstName, fields.lastName),
                address = fields.address,
                email = fields.email,
                phone = fields.phone,
                notes = fields.notes,
                created_at = timestamp,
                updated_at = timestamp,
            )
        }
        insertSignature(id, signature, timestamp)
        id
    }

    override suspend fun update(id: String, draft: PatientDraft): Result<Unit> = operation {
        patients.selectPatientById(id).executeAsOneOrNull() ?: throw PatientError.NotFound
        val fields = draft.normalized()
        ensureUnique(fields, ownId = id)
        writeOrReportDuplicate(fields, ownId = id) {
            patients.updatePatient(
                document_type = fields.documentType.name,
                document_number = fields.documentNumber,
                document_key = fields.documentKey,
                first_name = fields.firstName,
                last_name = fields.lastName,
                full_name_search = SearchText.fullName(fields.firstName, fields.lastName),
                address = fields.address,
                email = fields.email,
                phone = fields.phone,
                notes = fields.notes,
                updated_at = now(),
                id = id,
            )
        }
    }

    override suspend fun addSignature(id: String, signature: ConsentSignature): Result<Unit> = operation {
        patients.selectPatientById(id).executeAsOneOrNull() ?: throw PatientError.NotFound
        val timestamp = now()
        insertSignature(id, signature, timestamp)
        // observe() only listens to the patient table: touching the row refreshes the signature info.
        patients.touchPatient(updated_at = timestamp, id = id)
    }

    override fun observe(id: String): Flow<Patient?> =
        patients.selectPatientById(id).asFlow().mapToOneOrNull(dispatcher).map { row -> row?.toDomain() }

    // Re-emits after a new signature because the patient_signature table is part of the query.
    override fun observeLatestSignatureImage(id: String): Flow<ByteArray?> =
        signatures.selectLatestSignature(id).asFlow().mapToOneOrNull(dispatcher).map { it?.image_png }

    override fun search(
        criterion: PatientSearchCriterion,
        query: String,
        includeArchived: Boolean,
    ): Flow<List<PatientSummary>> {
        val archived = if (includeArchived) 1L else 0L
        val search = when (criterion) {
            PatientSearchCriterion.DOCUMENT -> {
                // Upper-case without spaces matches the stored key of every document type.
                val key = IdentityDocumentValidator.normalize(IdentityDocumentType.PASSPORT, query)
                patients.searchByDocument(contains(key), archived, SEARCH_LIMIT)
            }
            PatientSearchCriterion.FULL_NAME ->
                patients.searchByFullName(contains(SearchText.normalize(query)), archived, SEARCH_LIMIT)
            PatientSearchCriterion.EMAIL ->
                patients.searchByEmail(contains(EmailValidator.normalize(query)), archived, SEARCH_LIMIT)
        }
        return search.asFlow().mapToList(dispatcher).map { rows -> rows.map { it.toSummary() } }
    }

    override suspend fun setArchived(id: String, archived: Boolean): Result<Unit> = operation {
        patients.selectPatientById(id).executeAsOneOrNull() ?: throw PatientError.NotFound
        val timestamp = now()
        patients.setArchivedAt(archived_at = timestamp.takeIf { archived }, updated_at = timestamp, id = id)
    }

    override suspend fun delete(id: String): Result<Unit> = operation {
        patients.selectPatientById(id).executeAsOneOrNull() ?: throw PatientError.NotFound
        if (hasClinicalRecords(id)) throw PatientError.HasClinicalRecords
        patients.deletePatientById(id)
    }

    private fun insertSignature(patientId: String, signature: ConsentSignature, timestamp: Long) {
        val representative = signature.signer == SignatureSigner.REPRESENTATIVE
        signatures.insertSignature(
            id = randomUuid(),
            patient_id = patientId,
            image_png = signature.pngBytes,
            signer = signature.signer.name,
            representative_name = signature.representativeName.takeIf { representative },
            representative_document = signature.representativeDocument.takeIf { representative },
            consent_version = signature.consentVersion,
            captured_at = timestamp,
        )
    }

    private fun ensureUnique(fields: Fields, ownId: String?) {
        patients.selectPatientIdByDocument(fields.documentType.name, fields.documentKey).executeAsOneOrNull()?.let {
            if (it != ownId) throw PatientError.DocumentAlreadyRegistered
        }
    }

    /** A concurrent write may take the document after [ensureUnique]: report it the same way. */
    private fun writeOrReportDuplicate(fields: Fields, ownId: String, write: () -> Unit) {
        runCatching(write).onFailure { error ->
            ensureUnique(fields, ownId)
            throw error
        }
    }

    private fun hasClinicalRecords(id: String) = (patients.countClinicalRecords(id).executeAsOneOrNull() ?: 0L) > 0

    private fun PatientRow.toDomain() = Patient(
        id = id,
        documentType = documentType(),
        documentNumber = document_number,
        firstName = first_name,
        lastName = last_name,
        address = address,
        email = email,
        phone = phone,
        notes = notes,
        isArchived = archived_at != null,
        hasClinicalRecords = hasClinicalRecords(id),
        signature = signatures.selectLatestSignature(id).executeAsOneOrNull()?.let { latest ->
            SignatureInfo(
                signer = SignatureSigner.valueOf(latest.signer),
                representativeName = latest.representative_name,
                representativeDocument = latest.representative_document,
                consentVersion = latest.consent_version,
                capturedAtEpochMillis = latest.captured_at,
                totalSignatures = signatures.countSignatures(id).executeAsOne().toInt(),
            )
        },
    )

    /** Runs [block] in a transaction on [dispatcher]; [PatientError]s become failures. */
    private suspend fun <T> operation(block: () -> T): Result<T> = withContext(dispatcher) {
        try {
            Result.success(database.transactionWithResult { block() })
        } catch (error: CancellationException) {
            throw error
        } catch (error: PatientError) {
            Result.failure(error)
        } catch (@Suppress("TooGenericExceptionCaught") error: Exception) {
            Result.failure(PatientError.Unknown(error))
        }
    }

    // The normalized columns of one record: a plain holder, as long as the table row itself.
    @Suppress("LongParameterList")
    private class Fields(
        val documentType: IdentityDocumentType,
        val documentNumber: String,
        val documentKey: String,
        val firstName: String,
        val lastName: String,
        val address: String?,
        val email: String?,
        val phone: String?,
        val notes: String?,
    )

    private fun PatientDraft.normalized() = Fields(
        documentType = documentType,
        documentNumber = if (documentType == IdentityDocumentType.NIF_NIE) {
            IdentityDocumentValidator.normalize(documentType, documentNumber)
        } else {
            documentNumber.trim()
        },
        documentKey = IdentityDocumentValidator.normalize(documentType, documentNumber),
        firstName = firstName.trim(),
        lastName = lastName.trim(),
        address = address.trim().ifEmpty { null },
        email = email.trim().ifEmpty { null }?.let(EmailValidator::normalize),
        phone = phone.trim().ifEmpty { null }?.let(PhoneValidator::normalize),
        notes = notes.trim().ifEmpty { null },
    )

    private companion object {
        const val SEARCH_LIMIT = 200L
    }
}

private fun PatientRow.documentType(): IdentityDocumentType = IdentityDocumentType.valueOf(document_type)

private fun PatientRow.toSummary() = PatientSummary(
    id = id,
    firstName = first_name,
    lastName = last_name,
    documentType = documentType(),
    documentNumber = document_number,
    email = email,
    isArchived = archived_at != null,
)

/** LIKE patterns with `\` escaping the wildcards the user may type (see Patient.sq). */
private fun escapeLike(value: String) = value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")
private fun contains(value: String) = "%" + escapeLike(value) + "%"

@OptIn(ExperimentalUuidApi::class)
private fun randomUuid(): String = Uuid.random().toString()
