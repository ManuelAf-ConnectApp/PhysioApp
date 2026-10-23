package com.connectapp.domain.patients

import kotlinx.coroutines.flow.Flow

/**
 * Patient records and their signed consents in the local database (D13).
 * Inputs reach it already validated; the implementation normalizes documents, email and phone.
 * Failures are always [PatientError]s.
 */
interface PatientRepository {
    /** Stores the patient and its first signature together; returns the new id. */
    suspend fun create(draft: PatientDraft, signature: ConsentSignature): Result<String>

    /** Updates the data; never asks for a new signature (D13). */
    suspend fun update(id: String, draft: PatientDraft): Result<Unit>

    /** "Volver a firmar": adds a signature and keeps the earlier ones (D13). */
    suspend fun addSignature(id: String, signature: ConsentSignature): Result<Unit>

    /** The record, kept up to date, or null once it no longer exists. */
    fun observe(id: String): Flow<Patient?>

    /** PNG of the latest signature, kept up to date. */
    fun observeLatestSignatureImage(id: String): Flow<ByteArray?>

    /** Records matching [query] by [criterion], ordered by name; a blank query lists all (D8 for archived). */
    fun search(criterion: PatientSearchCriterion, query: String, includeArchived: Boolean): Flow<List<PatientSummary>>

    suspend fun setArchived(id: String, archived: Boolean): Result<Unit>

    /** Physical deletion, only without reports or invoices (D8). Its signatures go with it. */
    suspend fun delete(id: String): Result<Unit>
}
