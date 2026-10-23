package com.connectapp.domain.professionals

import kotlinx.coroutines.flow.Flow

/** "Registrar profesional". */
class CreateProfessionalUseCase(private val repository: ProfessionalRepository) {
    suspend operator fun invoke(draft: ProfessionalDraft): Result<String> =
        if (ProfessionalValidation.errors(draft).isNotEmpty()) {
            Result.failure(ProfessionalError.InvalidInput)
        } else {
            repository.create(draft.trimmed())
        }
}

/** Edit mode of the form: updates the same record, never creates a duplicate. */
class UpdateProfessionalUseCase(private val repository: ProfessionalRepository) {
    suspend operator fun invoke(id: String, draft: ProfessionalDraft): Result<Unit> =
        if (ProfessionalValidation.errors(draft).isNotEmpty()) {
            Result.failure(ProfessionalError.InvalidInput)
        } else {
            repository.update(id, draft.trimmed())
        }
}

class ObserveProfessionalUseCase(private val repository: ProfessionalRepository) {
    operator fun invoke(id: String): Flow<Professional?> = repository.observe(id)
}

class SearchProfessionalsUseCase(private val repository: ProfessionalRepository) {
    operator fun invoke(
        criterion: ProfessionalSearchCriterion,
        query: String,
        includeArchived: Boolean,
    ): Flow<List<ProfessionalSummary>> = repository.search(criterion, query.trim(), includeArchived)
}

/** "Borrar" archives (D8, confirmed 2026-10-20); archived records can be restored. */
class SetProfessionalArchivedUseCase(private val repository: ProfessionalRepository) {
    suspend operator fun invoke(id: String, archived: Boolean): Result<Unit> = repository.setArchived(id, archived)
}

class DeleteProfessionalUseCase(private val repository: ProfessionalRepository) {
    suspend operator fun invoke(id: String): Result<Unit> = repository.delete(id)
}

private fun ProfessionalDraft.trimmed() = copy(
    nif = nif.trim(),
    licenseNumber = licenseNumber.trim(),
    firstName = firstName.trim(),
    lastName = lastName.trim(),
    address = address.trim(),
    email = email.trim(),
    phone = phone.trim(),
    notes = notes.trim(),
)
