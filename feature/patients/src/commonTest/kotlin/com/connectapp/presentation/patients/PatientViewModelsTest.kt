package com.connectapp.presentation.patients

import androidx.compose.ui.geometry.Offset
import com.connectapp.domain.patients.AddPatientSignatureUseCase
import com.connectapp.domain.patients.CreatePatientUseCase
import com.connectapp.domain.patients.DeletePatientUseCase
import com.connectapp.domain.patients.ObservePatientSignatureImageUseCase
import com.connectapp.domain.patients.ObservePatientUseCase
import com.connectapp.domain.patients.PatientSearchCriterion
import com.connectapp.domain.patients.SearchPatientsUseCase
import com.connectapp.domain.patients.SetPatientArchivedUseCase
import com.connectapp.domain.patients.SignatureSigner
import com.connectapp.domain.patients.UpdatePatientUseCase
import com.connectapp.presentation.patients.consent.ConsentFormState
import com.connectapp.presentation.patients.detail.PatientDetailIntent
import com.connectapp.presentation.patients.detail.PatientDetailViewModel
import com.connectapp.presentation.patients.detail.PatientDialog
import com.connectapp.presentation.patients.form.PatientFormEffect
import com.connectapp.presentation.patients.form.PatientFormIntent
import com.connectapp.presentation.patients.form.PatientFormViewModel
import com.connectapp.presentation.patients.search.PatientSearchIntent
import com.connectapp.presentation.patients.search.PatientSearchViewModel
import com.connectapp.presentation.patients.sign.PatientSignIntent
import com.connectapp.presentation.patients.sign.PatientSignViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PatientViewModelsTest {

    private val dispatcher = StandardTestDispatcher()
    private val repository = FakePatientRepository()
    private val signed = ConsentFormState(strokes = listOf(listOf(Offset(0.1f, 0.5f), Offset(0.9f, 0.5f))))
    private val png = byteArrayOf(1, 2, 3)

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun form(id: String? = null) = PatientFormViewModel(
        id,
        CreatePatientUseCase(repository),
        UpdatePatientUseCase(repository),
        ObservePatientUseCase(repository),
    )

    private suspend fun <E> TestScope.effectsOf(flow: Flow<E>, block: () -> Unit): List<E> {
        val effects = mutableListOf<E>()
        val job = launch { flow.toList(effects) }
        advanceUntilIdle()
        block()
        advanceUntilIdle()
        job.cancel()
        return effects
    }

    // D13: the consent signature is mandatory to register a patient.
    @Test
    fun `a new patient cannot be saved until the consent is signed`() = runTest {
        val viewModel = form()
        viewModel.onIntent(PatientFormIntent.DraftChanged(pepeDraft))
        assertFalse(viewModel.state.value.canSubmit)

        val effects = effectsOf(viewModel.effect) {
            viewModel.onIntent(PatientFormIntent.ConsentChanged(signed))
            assertTrue(viewModel.state.value.canSubmit)
            viewModel.onIntent(PatientFormIntent.SaveClicked(png))
        }

        assertEquals(listOf<PatientFormEffect>(PatientFormEffect.Saved("pat-1")), effects)
        assertEquals(SignatureSigner.PATIENT, repository.signatures.getValue("pat-1").single().signer)
    }

    @Test
    fun `a representative must be identified before saving`() = runTest {
        val viewModel = form()
        viewModel.onIntent(PatientFormIntent.DraftChanged(pepeDraft))
        viewModel.onIntent(PatientFormIntent.ConsentChanged(signed.copy(signer = SignatureSigner.REPRESENTATIVE)))
        assertFalse(viewModel.state.value.canSubmit)

        viewModel.onIntent(
            PatientFormIntent.ConsentChanged(
                signed.copy(
                    signer = SignatureSigner.REPRESENTATIVE,
                    representativeName = "Rosa Ruiz",
                    representativeDocument = "12345678Z",
                ),
            ),
        )
        assertTrue(viewModel.state.value.canSubmit)
    }

    // "evitar perder cambios sin confirmación".
    @Test
    fun `unsaved changes are tracked until the record is saved`() = runTest {
        val viewModel = form()
        assertFalse(viewModel.state.value.hasUnsavedChanges)

        viewModel.onIntent(PatientFormIntent.ConsentChanged(signed))
        assertTrue(viewModel.state.value.hasUnsavedChanges)

        viewModel.onIntent(PatientFormIntent.DraftChanged(pepeDraft))
        viewModel.onIntent(PatientFormIntent.SaveClicked(png))
        advanceUntilIdle()
        assertFalse(viewModel.state.value.hasUnsavedChanges)
    }

    @Test
    fun `editing needs no signature and updates the same record`() = runTest {
        repository.add("pat-1")
        val viewModel = form("pat-1")
        advanceUntilIdle()
        assertFalse(viewModel.state.value.hasUnsavedChanges)
        assertTrue(viewModel.state.value.canSubmit)

        viewModel.onIntent(PatientFormIntent.DraftChanged(pepeDraft.copy(notes = "Lumbalgia")))
        assertTrue(viewModel.state.value.hasUnsavedChanges)
        viewModel.onIntent(PatientFormIntent.SaveClicked(ByteArray(0)))
        advanceUntilIdle()

        assertEquals("Lumbalgia", repository.records.value.getValue("pat-1").notes)
        assertTrue(repository.signatures["pat-1"].isNullOrEmpty())
    }

    @Test
    fun `signing again adds a signature`() = runTest {
        repository.add("pat-1")
        val viewModel = PatientSignViewModel("pat-1", AddPatientSignatureUseCase(repository))

        viewModel.onIntent(PatientSignIntent.ConsentChanged(signed))
        assertTrue(viewModel.state.value.hasUnsavedChanges)
        viewModel.onIntent(PatientSignIntent.SaveClicked(png))
        advanceUntilIdle()

        assertEquals(1, repository.signatures.getValue("pat-1").size)
        assertFalse(viewModel.state.value.hasUnsavedChanges)
    }

    @Test
    fun `changing the search criterion clears the query`() = runTest {
        val viewModel = PatientSearchViewModel(SearchPatientsUseCase(repository))
        viewModel.onIntent(PatientSearchIntent.QueryChanged("pepe"))

        viewModel.onIntent(PatientSearchIntent.CriterionSelected(PatientSearchCriterion.EMAIL))
        advanceUntilIdle()

        assertEquals(Triple(PatientSearchCriterion.EMAIL, "", false), repository.lastSearch)
    }

    @Test
    fun `archiving a patient needs confirmation and can be undone`() = runTest {
        repository.add("pat-1")
        val viewModel = PatientDetailViewModel(
            "pat-1",
            ObservePatientUseCase(repository),
            ObservePatientSignatureImageUseCase(repository),
            SetPatientArchivedUseCase(repository),
            DeletePatientUseCase(repository),
        )
        advanceUntilIdle()

        viewModel.onIntent(PatientDetailIntent.ArchiveClicked)
        assertEquals(PatientDialog.ARCHIVE, viewModel.state.value.dialog)
        viewModel.onIntent(PatientDetailIntent.DialogConfirmed)
        advanceUntilIdle()
        assertTrue(viewModel.state.value.patient?.isArchived == true)

        viewModel.onIntent(PatientDetailIntent.UnarchiveClicked)
        advanceUntilIdle()
        assertFalse(viewModel.state.value.patient?.isArchived == true)
    }
}
