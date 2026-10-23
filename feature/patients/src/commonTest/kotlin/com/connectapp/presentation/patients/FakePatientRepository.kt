package com.connectapp.presentation.patients

import com.connectapp.domain.patients.ConsentSignature
import com.connectapp.domain.patients.Patient
import com.connectapp.domain.patients.PatientDraft
import com.connectapp.domain.patients.PatientError
import com.connectapp.domain.patients.PatientRepository
import com.connectapp.domain.patients.PatientSearchCriterion
import com.connectapp.domain.patients.PatientSummary
import com.connectapp.domain.patients.SignatureInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

internal val pepeDraft = PatientDraft(documentNumber = "X1234567L", firstName = "Pepe", lastName = "Ruiz")

/** In-memory [PatientRepository] that records the signatures it receives. */
internal class FakePatientRepository : PatientRepository {
    val records = MutableStateFlow<Map<String, Patient>>(emptyMap())
    val signatures = mutableMapOf<String, MutableList<ConsentSignature>>()
    var lastSearch: Triple<PatientSearchCriterion, String, Boolean>? = null
        private set

    fun add(id: String, draft: PatientDraft = pepeDraft, archived: Boolean = false) {
        records.value += id to draft.toPatient(id, archived)
    }

    override suspend fun create(draft: PatientDraft, signature: ConsentSignature): Result<String> {
        val id = "pat-${records.value.size + 1}"
        signatures.getOrPut(id) { mutableListOf() } += signature
        add(id, draft)
        return Result.success(id)
    }

    override suspend fun update(id: String, draft: PatientDraft): Result<Unit> {
        val current = records.value[id] ?: return Result.failure(PatientError.NotFound)
        records.value += id to draft.toPatient(id, current.isArchived)
        return Result.success(Unit)
    }

    override suspend fun addSignature(id: String, signature: ConsentSignature): Result<Unit> {
        signatures.getOrPut(id) { mutableListOf() } += signature
        return Result.success(Unit)
    }

    override fun observe(id: String): Flow<Patient?> = records.map { it[id] }

    override fun observeLatestSignatureImage(id: String): Flow<ByteArray?> =
        records.map { signatures[id]?.lastOrNull()?.pngBytes }

    override fun search(
        criterion: PatientSearchCriterion,
        query: String,
        includeArchived: Boolean,
    ): Flow<List<PatientSummary>> {
        lastSearch = Triple(criterion, query, includeArchived)
        return records.map { all ->
            all.values
                .filter { includeArchived || !it.isArchived }
                .filter { "${it.firstName} ${it.lastName}".contains(query, ignoreCase = true) }
                .map { PatientSummary(it.id, it.firstName, it.lastName, it.documentType, it.documentNumber, it.email, it.isArchived) }
        }
    }

    override suspend fun setArchived(id: String, archived: Boolean): Result<Unit> {
        val current = records.value[id] ?: return Result.failure(PatientError.NotFound)
        records.value += id to current.copy(isArchived = archived)
        return Result.success(Unit)
    }

    override suspend fun delete(id: String): Result<Unit> {
        records.value -= id
        return Result.success(Unit)
    }

    private fun PatientDraft.toPatient(id: String, archived: Boolean) = Patient(
        id = id,
        documentType = documentType,
        documentNumber = documentNumber,
        firstName = firstName,
        lastName = lastName,
        address = address.ifBlank { null },
        email = email.ifBlank { null },
        phone = phone.ifBlank { null },
        notes = notes.ifBlank { null },
        isArchived = archived,
        hasClinicalRecords = false,
        signature = signatures[id]?.lastOrNull()?.let {
            SignatureInfo(it.signer, null, null, it.consentVersion, 0L, signatures.getValue(id).size)
        },
    )
}
