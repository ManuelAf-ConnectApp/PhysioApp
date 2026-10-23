package com.connectapp.domain.professionals

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ProfessionalUseCasesTest {

    private val repository = RecordingRepository()

    @Test
    fun `an invalid draft never reaches the repository`() = runTest {
        val result = CreateProfessionalUseCase(repository)(validDraft.copy(nif = "12345678A"))

        assertEquals(ProfessionalError.InvalidInput, result.exceptionOrNull())
        assertNull(repository.lastDraft)
    }

    @Test
    fun `a valid draft is stored trimmed`() = runTest {
        CreateProfessionalUseCase(repository)(validDraft.copy(firstName = "  Ana ", notes = " nota "))

        assertEquals("Ana", repository.lastDraft?.firstName)
        assertEquals("nota", repository.lastDraft?.notes)
    }

    @Test
    fun `editing updates the given record`() = runTest {
        UpdateProfessionalUseCase(repository)("pro-1", validDraft)

        assertEquals("pro-1", repository.lastUpdatedId)
    }

    private class RecordingRepository : ProfessionalRepository {
        var lastDraft: ProfessionalDraft? = null
        var lastUpdatedId: String? = null

        override suspend fun create(draft: ProfessionalDraft): Result<String> {
            lastDraft = draft
            return Result.success("pro-new")
        }

        override suspend fun update(id: String, draft: ProfessionalDraft): Result<Unit> {
            lastUpdatedId = id
            lastDraft = draft
            return Result.success(Unit)
        }

        override fun observe(id: String): Flow<Professional?> = emptyFlow()

        override fun search(
            criterion: ProfessionalSearchCriterion,
            query: String,
            includeArchived: Boolean,
        ): Flow<List<ProfessionalSummary>> = emptyFlow()

        override suspend fun setArchived(id: String, archived: Boolean) = Result.success(Unit)

        override suspend fun delete(id: String) = Result.success(Unit)
    }
}
