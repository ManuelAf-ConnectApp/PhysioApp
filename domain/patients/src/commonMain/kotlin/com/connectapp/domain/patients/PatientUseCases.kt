package com.connectapp.domain.patients

import kotlinx.coroutines.flow.Flow

/** "Registrar paciente" with its mandatory consent signature (D13). */
class CreatePatientUseCase(private val repository: PatientRepository) {
    suspend operator fun invoke(draft: PatientDraft, signature: ConsentSignature): Result<String> =
        if (PatientValidation.errors(draft).isNotEmpty() || PatientValidation.signatureErrors(signature).isNotEmpty()) {
            Result.failure(PatientError.InvalidInput)
        } else {
            repository.create(draft.trimmed(), signature.trimmed())
        }
}

/** Edit mode of the form: updates the same record, never creates a duplicate. */
class UpdatePatientUseCase(private val repository: PatientRepository) {
    suspend operator fun invoke(id: String, draft: PatientDraft): Result<Unit> =
        if (PatientValidation.errors(draft).isNotEmpty()) {
            Result.failure(PatientError.InvalidInput)
        } else {
            repository.update(id, draft.trimmed())
        }
}

/** "Volver a firmar" (D13). */
class AddPatientSignatureUseCase(private val repository: PatientRepository) {
    suspend operator fun invoke(id: String, signature: ConsentSignature): Result<Unit> =
        if (PatientValidation.signatureErrors(signature).isNotEmpty()) {
            Result.failure(PatientError.InvalidInput)
        } else {
            repository.addSignature(id, signature.trimmed())
        }
}

class ObservePatientUseCase(private val repository: PatientRepository) {
    operator fun invoke(id: String): Flow<Patient?> = repository.observe(id)
}

class ObservePatientSignatureImageUseCase(private val repository: PatientRepository) {
    operator fun invoke(id: String): Flow<ByteArray?> = repository.observeLatestSignatureImage(id)
}

class SearchPatientsUseCase(private val repository: PatientRepository) {
    operator fun invoke(
        criterion: PatientSearchCriterion,
        query: String,
        includeArchived: Boolean,
    ): Flow<List<PatientSummary>> =
        repository.search(criterion, query.trim(), includeArchived)
}

/** "Borrar" archives (D8, confirmed 2026-10-20); archived records can be restored. */
class SetPatientArchivedUseCase(private val repository: PatientRepository) {
    suspend operator fun invoke(id: String, archived: Boolean): Result<Unit> = repository.setArchived(id, archived)
}

class DeletePatientUseCase(private val repository: PatientRepository) {
    suspend operator fun invoke(id: String): Result<Unit> = repository.delete(id)
}

private fun PatientDraft.trimmed() = copy(
    documentNumber = documentNumber.trim(),
    firstName = firstName.trim(),
    lastName = lastName.trim(),
    address = address.trim(),
    email = email.trim(),
    phone = phone.trim(),
    notes = notes.trim(),
)

private fun ConsentSignature.trimmed() = if (signer == SignatureSigner.PATIENT) {
    ConsentSignature(pngBytes, signer, consentVersion = consentVersion)
} else {
    ConsentSignature(pngBytes, signer, representativeName.trim(), representativeDocument.trim(), consentVersion)
}
