package com.connectapp.data.professionals

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.connectapp.core.database.physio.PhysioDatabase
import com.connectapp.domain.professionals.Professional
import com.connectapp.domain.professionals.ProfessionalDraft
import com.connectapp.domain.professionals.ProfessionalError
import com.connectapp.domain.professionals.ProfessionalRepository
import com.connectapp.domain.professionals.ProfessionalSearchCriterion
import com.connectapp.domain.professionals.ProfessionalSummary
import com.connectapp.domain.shared.professional.ProfessionalCollege
import com.connectapp.domain.shared.text.SearchText
import com.connectapp.domain.shared.validation.EmailValidator
import com.connectapp.domain.shared.validation.LicenseNumberValidator
import com.connectapp.domain.shared.validation.PhoneValidator
import com.connectapp.domain.shared.validation.SpanishIdValidator
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import com.connectapp.core.database.Professional as ProfessionalRow

/**
 * Professional records over the encrypted [PhysioDatabase] (phase 6).
 *
 * - Uniqueness (D9): NIF, and licence number within its college. Violations are reported as
 *   [ProfessionalError.NifAlreadyRegistered] / [ProfessionalError.LicenseNumberAlreadyRegistered].
 * - Deletion (D8): archiving is the normal "delete"; physical deletion only for records that no
 *   account, report or invoice points at. Records with an account are never archived either.
 */
class ProfessionalRepositoryImpl(
    private val database: PhysioDatabase,
    private val now: () -> Long,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ProfessionalRepository {

    private val queries = database.professionalQueries

    override suspend fun create(draft: ProfessionalDraft): Result<String> = operation {
        val fields = draft.normalized()
        ensureUnique(fields, ownId = null)
        val id = randomUuid()
        val timestamp = now()
        insertOrReportDuplicate(fields, ownId = id) {
            queries.insertProfessional(
                id = id,
                nif = fields.nif,
                college_id = fields.college.name,
                license_number = fields.licenseNumber,
                license_number_key = fields.licenseKey,
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
        id
    }

    override suspend fun update(id: String, draft: ProfessionalDraft): Result<Unit> = operation {
        queries.selectProfessionalById(id).executeAsOneOrNull() ?: throw ProfessionalError.NotFound
        val fields = draft.normalized()
        ensureUnique(fields, ownId = id)
        insertOrReportDuplicate(fields, ownId = id) {
            queries.updateProfessional(
                nif = fields.nif,
                college_id = fields.college.name,
                license_number = fields.licenseNumber,
                license_number_key = fields.licenseKey,
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

    override fun observe(id: String): Flow<Professional?> =
        queries.selectProfessionalById(id).asFlow().mapToOneOrNull(dispatcher).map { row -> row?.toDomain() }

    override fun search(
        criterion: ProfessionalSearchCriterion,
        query: String,
        includeArchived: Boolean,
    ): Flow<List<ProfessionalSummary>> {
        val archived = if (includeArchived) 1L else 0L
        val search = when (criterion) {
            ProfessionalSearchCriterion.NIF ->
                queries.searchByNif(startsWith(SpanishIdValidator.normalize(query)), archived, SEARCH_LIMIT)
            ProfessionalSearchCriterion.FULL_NAME ->
                queries.searchByFullName(contains(SearchText.normalize(query)), archived, SEARCH_LIMIT)
            ProfessionalSearchCriterion.LICENSE_NUMBER ->
                queries.searchByLicense(contains(LicenseNumberValidator.normalize(query)), archived, SEARCH_LIMIT)
        }
        return search.asFlow().mapToList(dispatcher).map { rows -> rows.map { it.toSummary() } }
    }

    override suspend fun setArchived(id: String, archived: Boolean): Result<Unit> = operation {
        queries.selectProfessionalById(id).executeAsOneOrNull() ?: throw ProfessionalError.NotFound
        if (archived && hasAccount(id)) throw ProfessionalError.HasAccount
        val timestamp = now()
        queries.setArchivedAt(archived_at = timestamp.takeIf { archived }, updated_at = timestamp, id = id)
    }

    override suspend fun delete(id: String): Result<Unit> = operation {
        queries.selectProfessionalById(id).executeAsOneOrNull() ?: throw ProfessionalError.NotFound
        if (hasAccount(id)) throw ProfessionalError.HasAccount
        if (hasClinicalRecords(id)) throw ProfessionalError.HasClinicalRecords
        queries.deleteProfessionalById(id)
    }

    private fun ensureUnique(fields: Fields, ownId: String?) {
        queries.selectProfessionalByNif(fields.nif).executeAsOneOrNull()?.let {
            if (it.id != ownId) throw ProfessionalError.NifAlreadyRegistered
        }
        queries.selectProfessionalIdByLicense(fields.college.name, fields.licenseKey).executeAsOneOrNull()?.let {
            if (it != ownId) throw ProfessionalError.LicenseNumberAlreadyRegistered
        }
    }

    /** A concurrent write may take the NIF or licence after [ensureUnique]: report it the same way. */
    private fun insertOrReportDuplicate(fields: Fields, ownId: String, write: () -> Unit) {
        runCatching(write).onFailure { error ->
            ensureUnique(fields, ownId)
            throw error
        }
    }

    private fun hasAccount(id: String) = queries.countLinkedAccounts(id).executeAsOne() > 0
    private fun hasClinicalRecords(id: String) = (queries.countClinicalRecords(id).executeAsOneOrNull() ?: 0L) > 0

    private fun ProfessionalRow.toDomain() = Professional(
        id = id,
        nif = nif,
        college = college(),
        licenseNumber = license_number,
        firstName = first_name,
        lastName = last_name,
        address = address,
        email = email,
        phone = phone,
        notes = notes,
        isArchived = archived_at != null,
        hasAccount = hasAccount(id),
        hasClinicalRecords = hasClinicalRecords(id),
    )

    /** Runs [block] on [dispatcher]; [ProfessionalError]s become failures, anything else Unknown. */
    private suspend fun <T> operation(block: () -> T): Result<T> = withContext(dispatcher) {
        try {
            Result.success(database.transactionWithResult { block() })
        } catch (error: CancellationException) {
            throw error
        } catch (error: ProfessionalError) {
            Result.failure(error)
        } catch (@Suppress("TooGenericExceptionCaught") error: Exception) {
            Result.failure(ProfessionalError.Unknown(error))
        }
    }

    // The normalized columns of one record: a plain holder, as long as the table row itself.
    @Suppress("LongParameterList")
    private class Fields(
        val nif: String,
        val college: ProfessionalCollege,
        val licenseNumber: String,
        val licenseKey: String,
        val firstName: String,
        val lastName: String,
        val address: String?,
        val email: String?,
        val phone: String?,
        val notes: String?,
    )

    private fun ProfessionalDraft.normalized() = Fields(
        nif = SpanishIdValidator.normalize(nif),
        college = college ?: throw ProfessionalError.InvalidInput,
        licenseNumber = licenseNumber.trim(),
        licenseKey = LicenseNumberValidator.normalize(licenseNumber),
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

private fun ProfessionalRow.college(): ProfessionalCollege =
    checkNotNull(ProfessionalCollege.fromId(college_id)) { "Unknown college id" }

private fun ProfessionalRow.toSummary() = ProfessionalSummary(
    id = id,
    firstName = first_name,
    lastName = last_name,
    nif = nif,
    college = college(),
    licenseNumber = license_number,
    isArchived = archived_at != null,
)

/** LIKE patterns with `\` escaping the wildcards the user may type (see Professional.sq). */
private fun escapeLike(value: String) = value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")
private fun startsWith(value: String) = escapeLike(value) + "%"
private fun contains(value: String) = "%" + escapeLike(value) + "%"

@OptIn(ExperimentalUuidApi::class)
private fun randomUuid(): String = Uuid.random().toString()
