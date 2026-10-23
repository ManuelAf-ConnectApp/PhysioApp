package com.connectapp.domain.professionals

import kotlinx.coroutines.flow.Flow

/**
 * Professional records in the local database. Drafts reach it already
 * validated; the implementation normalizes NIF, licence number, email and phone itself. Failures
 * are always [ProfessionalError]s.
 */
interface ProfessionalRepository {
    /** Returns the id of the new record. */
    suspend fun create(draft: ProfessionalDraft): Result<String>

    suspend fun update(id: String, draft: ProfessionalDraft): Result<Unit>

    /** The record, kept up to date, or null once it no longer exists. */
    fun observe(id: String): Flow<Professional?>

    /**
     * Records matching [query] by [criterion], ordered by name. A blank query lists every record.
     * Archived records are only included with [includeArchived] (D8).
     */
    fun search(
        criterion: ProfessionalSearchCriterion,
        query: String,
        includeArchived: Boolean,
    ): Flow<List<ProfessionalSummary>>

    /**
     * Archives or restores the record (D8). Fails with [ProfessionalError.HasAccount] when
     * archiving one with an account.
     */
    suspend fun setArchived(id: String, archived: Boolean): Result<Unit>

    /** Physical deletion, only for records without account, reports or invoices (D8). */
    suspend fun delete(id: String): Result<Unit>
}
