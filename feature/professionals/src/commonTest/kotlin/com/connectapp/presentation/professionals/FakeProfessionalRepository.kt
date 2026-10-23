package com.connectapp.presentation.professionals

import com.connectapp.domain.professionals.Professional
import com.connectapp.domain.professionals.ProfessionalDraft
import com.connectapp.domain.professionals.ProfessionalError
import com.connectapp.domain.professionals.ProfessionalRepository
import com.connectapp.domain.professionals.ProfessionalSearchCriterion
import com.connectapp.domain.professionals.ProfessionalSummary
import com.connectapp.domain.shared.professional.ProfessionalCollege
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

internal val anaDraft = ProfessionalDraft(
    nif = "12345678Z",
    college = ProfessionalCollege.MADRID,
    licenseNumber = "28/1234",
    firstName = "Ana",
    lastName = "Gil",
)

/** In-memory [ProfessionalRepository]: the search matches names only, enough for the screens. */
internal class FakeProfessionalRepository : ProfessionalRepository {
    val records = MutableStateFlow<Map<String, Professional>>(emptyMap())
    var createResult: Result<String>? = null
    var lastSearch: Triple<ProfessionalSearchCriterion, String, Boolean>? = null
        private set

    fun add(id: String, draft: ProfessionalDraft = anaDraft, archived: Boolean = false, hasAccount: Boolean = false) {
        records.value += id to draft.toProfessional(id, archived, hasAccount)
    }

    override suspend fun create(draft: ProfessionalDraft): Result<String> {
        createResult?.let { return it }
        val id = "pro-${records.value.size + 1}"
        add(id, draft)
        return Result.success(id)
    }

    override suspend fun update(id: String, draft: ProfessionalDraft): Result<Unit> {
        val current = records.value[id] ?: return Result.failure(ProfessionalError.NotFound)
        records.value += id to draft.toProfessional(id, current.isArchived, current.hasAccount)
        return Result.success(Unit)
    }

    override fun observe(id: String): Flow<Professional?> = records.map { it[id] }

    override fun search(
        criterion: ProfessionalSearchCriterion,
        query: String,
        includeArchived: Boolean,
    ): Flow<List<ProfessionalSummary>> {
        lastSearch = Triple(criterion, query, includeArchived)
        return records.map { all ->
            all.values
                .filter { includeArchived || !it.isArchived }
                .filter { "${it.firstName} ${it.lastName}".contains(query, ignoreCase = true) }
                .map { ProfessionalSummary(it.id, it.firstName, it.lastName, it.nif, it.college, it.licenseNumber, it.isArchived) }
        }
    }

    override suspend fun setArchived(id: String, archived: Boolean): Result<Unit> {
        val current = records.value[id] ?: return Result.failure(ProfessionalError.NotFound)
        if (archived && current.hasAccount) return Result.failure(ProfessionalError.HasAccount)
        records.value += id to current.copy(isArchived = archived)
        return Result.success(Unit)
    }

    override suspend fun delete(id: String): Result<Unit> {
        records.value -= id
        return Result.success(Unit)
    }

    private fun ProfessionalDraft.toProfessional(id: String, archived: Boolean, hasAccount: Boolean) = Professional(
        id = id,
        nif = nif,
        college = checkNotNull(college),
        licenseNumber = licenseNumber,
        firstName = firstName,
        lastName = lastName,
        address = address.ifBlank { null },
        email = email.ifBlank { null },
        phone = phone.ifBlank { null },
        notes = notes.ifBlank { null },
        isArchived = archived,
        hasAccount = hasAccount,
        hasClinicalRecords = false,
    )
}
