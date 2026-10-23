package com.connectapp.presentation.professionals

import com.connectapp.commonresources.error_nif_already_registered
import com.connectapp.domain.professionals.CreateProfessionalUseCase
import com.connectapp.domain.professionals.DeleteProfessionalUseCase
import com.connectapp.domain.professionals.ObserveProfessionalUseCase
import com.connectapp.domain.professionals.ProfessionalError
import com.connectapp.domain.professionals.ProfessionalSearchCriterion
import com.connectapp.domain.professionals.SearchProfessionalsUseCase
import com.connectapp.domain.professionals.SetProfessionalArchivedUseCase
import com.connectapp.domain.professionals.UpdateProfessionalUseCase
import com.connectapp.presentation.professionals.detail.ProfessionalDetailEffect
import com.connectapp.presentation.professionals.detail.ProfessionalDetailIntent
import com.connectapp.presentation.professionals.detail.ProfessionalDetailViewModel
import com.connectapp.presentation.professionals.detail.ProfessionalDialog
import com.connectapp.presentation.professionals.form.ProfessionalFormEffect
import com.connectapp.presentation.professionals.form.ProfessionalFormIntent
import com.connectapp.presentation.professionals.form.ProfessionalFormViewModel
import com.connectapp.presentation.professionals.search.ProfessionalSearchIntent
import com.connectapp.presentation.professionals.search.ProfessionalSearchViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ProfessionalViewModelsTest {

    private val dispatcher = StandardTestDispatcher()
    private val repository = FakeProfessionalRepository()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun form(id: String? = null) = ProfessionalFormViewModel(
        id,
        CreateProfessionalUseCase(repository),
        UpdateProfessionalUseCase(repository),
        ObserveProfessionalUseCase(repository),
    )

    private fun detail(id: String) = ProfessionalDetailViewModel(
        id,
        ObserveProfessionalUseCase(repository),
        SetProfessionalArchivedUseCase(repository),
        DeleteProfessionalUseCase(repository),
    )

    private suspend fun <E> TestScope.effectsOf(flow: kotlinx.coroutines.flow.Flow<E>, block: () -> Unit): List<E> {
        val effects = mutableListOf<E>()
        val job = launch { flow.toList(effects) }
        advanceUntilIdle()
        block()
        advanceUntilIdle()
        job.cancel()
        return effects
    }

    @Test
    fun `the form saves a valid new record and reports its id`() = runTest {
        val viewModel = form()
        assertFalse(viewModel.state.value.canSubmit)

        val effects = effectsOf(viewModel.effect) {
            viewModel.onIntent(ProfessionalFormIntent.DraftChanged(anaDraft))
            assertTrue(viewModel.state.value.canSubmit)
            viewModel.onIntent(ProfessionalFormIntent.SaveClicked)
        }

        assertEquals(listOf<ProfessionalFormEffect>(ProfessionalFormEffect.Saved("pro-1")), effects)
    }

    @Test
    fun `a duplicate NIF keeps the form open with its message`() = runTest {
        repository.createResult = Result.failure(ProfessionalError.NifAlreadyRegistered)
        val viewModel = form()

        val effects = effectsOf(viewModel.effect) {
            viewModel.onIntent(ProfessionalFormIntent.DraftChanged(anaDraft))
            viewModel.onIntent(ProfessionalFormIntent.SaveClicked)
        }

        assertTrue(effects.isEmpty())
        assertEquals(error_nif_already_registered, viewModel.state.value.errorMessage)
    }

    // Edit opens the form prefilled and updates the same record.
    @Test
    fun `edit mode is prefilled and updates the same record`() = runTest {
        repository.add("pro-1")
        val viewModel = form("pro-1")
        advanceUntilIdle()
        assertEquals(anaDraft, viewModel.state.value.draft)
        assertTrue(viewModel.state.value.isEditing)

        viewModel.onIntent(ProfessionalFormIntent.DraftChanged(anaDraft.copy(firstName = "Ana María")))
        viewModel.onIntent(ProfessionalFormIntent.SaveClicked)
        advanceUntilIdle()

        assertEquals(1, repository.records.value.size)
        assertEquals("Ana María", repository.records.value.getValue("pro-1").firstName)
    }

    @Test
    fun `changing the search criterion clears the query`() = runTest {
        val viewModel = ProfessionalSearchViewModel(SearchProfessionalsUseCase(repository))
        viewModel.onIntent(ProfessionalSearchIntent.QueryChanged("ana"))

        viewModel.onIntent(ProfessionalSearchIntent.CriterionSelected(ProfessionalSearchCriterion.NIF))
        advanceUntilIdle()

        assertEquals("", viewModel.state.value.query)
        assertEquals(Triple(ProfessionalSearchCriterion.NIF, "", false), repository.lastSearch)
    }

    @Test
    fun `search results follow the query and the archived switch`() = runTest {
        repository.add("pro-1")
        repository.add("pro-2", anaDraft.copy(nif = "X1234567L", firstName = "Íñigo"), archived = true)
        val viewModel = ProfessionalSearchViewModel(SearchProfessionalsUseCase(repository))
        advanceUntilIdle()
        assertEquals(listOf("pro-1"), viewModel.state.value.results.map { it.id })

        viewModel.onIntent(ProfessionalSearchIntent.IncludeArchivedChanged(true))
        advanceUntilIdle()
        assertEquals(setOf("pro-1", "pro-2"), viewModel.state.value.results.map { it.id }.toSet())

        viewModel.onIntent(ProfessionalSearchIntent.QueryChanged("íñigo"))
        advanceUntilIdle()
        assertEquals(listOf("pro-2"), viewModel.state.value.results.map { it.id })
    }

    @Test
    fun `a selection that disappears from the results is cleared`() = runTest {
        repository.add("pro-1")
        val viewModel = ProfessionalSearchViewModel(SearchProfessionalsUseCase(repository))
        viewModel.onIntent(ProfessionalSearchIntent.ResultSelected("pro-1"))
        advanceUntilIdle()

        repository.setArchived("pro-1", true)
        advanceUntilIdle()

        assertNull(viewModel.state.value.selectedId)
    }

    // D8: archiving asks for confirmation and can be undone.
    @Test
    fun `archiving needs confirmation and restoring does not`() = runTest {
        repository.add("pro-1")
        val viewModel = detail("pro-1")
        advanceUntilIdle()

        viewModel.onIntent(ProfessionalDetailIntent.ArchiveClicked)
        assertEquals(ProfessionalDialog.ARCHIVE, viewModel.state.value.dialog)
        assertFalse(repository.records.value.getValue("pro-1").isArchived)

        viewModel.onIntent(ProfessionalDetailIntent.DialogConfirmed)
        advanceUntilIdle()
        assertTrue(viewModel.state.value.professional?.isArchived == true)

        viewModel.onIntent(ProfessionalDetailIntent.UnarchiveClicked)
        advanceUntilIdle()
        assertFalse(viewModel.state.value.professional?.isArchived == true)
    }

    @Test
    fun `dismissing the delete dialog keeps the record and confirming deletes it`() = runTest {
        repository.add("pro-1")
        val viewModel = detail("pro-1")

        val effects = effectsOf(viewModel.effect) {
            viewModel.onIntent(ProfessionalDetailIntent.DeleteClicked)
            viewModel.onIntent(ProfessionalDetailIntent.DialogDismissed)
        }
        assertTrue(effects.isEmpty())
        assertTrue("pro-1" in repository.records.value)

        val deleted = effectsOf(viewModel.effect) {
            viewModel.onIntent(ProfessionalDetailIntent.DeleteClicked)
            viewModel.onIntent(ProfessionalDetailIntent.DialogConfirmed)
        }
        assertEquals(listOf<ProfessionalDetailEffect>(ProfessionalDetailEffect.Deleted), deleted)
        assertFalse("pro-1" in repository.records.value)
    }
}
