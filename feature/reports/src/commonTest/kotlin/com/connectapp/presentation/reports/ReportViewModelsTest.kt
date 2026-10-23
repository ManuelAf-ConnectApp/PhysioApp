package com.connectapp.presentation.reports

import com.connectapp.commonresources.error_report_not_author
import com.connectapp.domain.model.ProfessionalProfile
import com.connectapp.domain.reports.AnnulReportUseCase
import com.connectapp.domain.reports.CreateReportUseCase
import com.connectapp.domain.reports.ObserveReportUseCase
import com.connectapp.domain.reports.ObserveReportVersionsUseCase
import com.connectapp.domain.reports.ReportSearchCriterion
import com.connectapp.domain.reports.SearchReportsUseCase
import com.connectapp.domain.reports.SuggestPatientsUseCase
import com.connectapp.domain.reports.UpdateReportUseCase
import com.connectapp.domain.shared.professional.ProfessionalCollege
import com.connectapp.domain.usecase.CurrentAuthorUseCase
import com.connectapp.domain.usecase.ObserveCurrentProfessionalUseCase
import com.connectapp.presentation.reports.detail.ReportDetailIntent
import com.connectapp.presentation.reports.detail.ReportDetailViewModel
import com.connectapp.presentation.reports.fake.FakeAuthRepository
import com.connectapp.presentation.reports.form.ReportFormEffect
import com.connectapp.presentation.reports.form.ReportFormIntent
import com.connectapp.presentation.reports.form.ReportFormViewModel
import com.connectapp.presentation.reports.search.ReportSearchIntent
import com.connectapp.presentation.reports.search.ReportSearchViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
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
class ReportViewModelsTest {

    private val dispatcher = StandardTestDispatcher()
    private val auth = FakeAuthRepository().apply {
        professional.value = ProfessionalProfile("pro-ana", "12345678Z", ProfessionalCollege.MADRID, "1", "Ana", "Gil")
    }
    private val reports = FakeReportRepository()
    private val observeProfessional = ObserveCurrentProfessionalUseCase(auth)
    private val author = CurrentAuthorUseCase(observeProfessional)

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun form(id: String? = null) = ReportFormViewModel(
        id,
        CreateReportUseCase(reports, author),
        UpdateReportUseCase(reports, author),
        SuggestPatientsUseCase(reports),
        ObserveReportUseCase(reports),
        author,
        observeProfessional,
    )

    private fun detail(id: String) = ReportDetailViewModel(
        id,
        ObserveReportUseCase(reports),
        ObserveReportVersionsUseCase(reports),
        author,
        AnnulReportUseCase(reports, author),
    )

    // The professional comes from the session and the patient from the picker.
    @Test
    fun `a new report is written by the session professional about a picked patient`() = runTest {
        val viewModel = form()
        val effects = mutableListOf<ReportFormEffect>()
        val job = launch { viewModel.effect.toList(effects) }
        advanceUntilIdle()
        assertEquals("Ana Gil", viewModel.state.value.authorName)

        viewModel.onIntent(ReportFormIntent.PatientQueryChanged("pep"))
        advanceUntilIdle()
        assertEquals(listOf(pepe), viewModel.state.value.suggestions)
        viewModel.onIntent(ReportFormIntent.PatientSelected(pepe))
        viewModel.onIntent(ReportFormIntent.ReportTextChanged("Datos"))
        assertFalse(viewModel.state.value.canSubmit)
        viewModel.onIntent(ReportFormIntent.ConditionTextChanged("Lumbalgia"))
        assertTrue(viewModel.state.value.hasUnsavedChanges)
        viewModel.onIntent(ReportFormIntent.SaveClicked)
        advanceUntilIdle()

        assertEquals(listOf<ReportFormEffect>(ReportFormEffect.Saved("rep-1")), effects)
        assertEquals("pro-ana", reports.reports.value.getValue("rep-1").professional.id)
        assertFalse(viewModel.state.value.hasUnsavedChanges)
        job.cancel()
    }

    // D17: only the author edits.
    @Test
    fun `another professional's report opens blocked`() = runTest {
        reports.add("rep-1", authorId = "pro-luis")
        val viewModel = form("rep-1")
        advanceUntilIdle()

        assertEquals(error_report_not_author, viewModel.state.value.blockedReason)
        assertFalse(viewModel.state.value.canSubmit)
    }

    @Test
    fun `the author's edit keeps a version`() = runTest {
        reports.add("rep-1", authorId = "pro-ana")
        val viewModel = form("rep-1")
        advanceUntilIdle()
        assertNull(viewModel.state.value.blockedReason)

        viewModel.onIntent(ReportFormIntent.ReportTextChanged("Datos corregidos"))
        viewModel.onIntent(ReportFormIntent.SaveClicked)
        advanceUntilIdle()

        assertEquals("Datos corregidos", reports.reports.value.getValue("rep-1").reportText)
        assertEquals(1, reports.versions.getValue("rep-1").size)
    }

    @Test
    fun `only the author may annul and a reason is required`() = runTest {
        reports.add("mine", authorId = "pro-ana")
        reports.add("theirs", authorId = "pro-luis")
        val mine = detail("mine")
        val theirs = detail("theirs")
        advanceUntilIdle()
        assertTrue(mine.state.value.canChange)
        assertFalse(theirs.state.value.canChange)

        mine.onIntent(ReportDetailIntent.AnnulClicked)
        assertFalse(mine.state.value.canConfirmAnnulment)
        mine.onIntent(ReportDetailIntent.AnnulmentReasonChanged("Paciente equivocado"))
        mine.onIntent(ReportDetailIntent.AnnulmentConfirmed)
        advanceUntilIdle()

        assertEquals("Paciente equivocado", mine.state.value.report?.annulment?.reason)
        assertFalse(mine.state.value.canChange)
    }

    // D17: anyone may export, author or not.
    @Test
    fun `any professional can open the PDF export`() = runTest {
        reports.add("theirs", authorId = "pro-luis")
        val viewModel = detail("theirs")
        advanceUntilIdle()

        viewModel.onIntent(ReportDetailIntent.ExportClicked)
        assertTrue(viewModel.state.value.isExporting)
        viewModel.onIntent(ReportDetailIntent.ExportDismissed)
        assertFalse(viewModel.state.value.isExporting)
    }

    @Test
    fun `changing the search criterion clears the query`() = runTest {
        val viewModel = ReportSearchViewModel(SearchReportsUseCase(reports))
        viewModel.onIntent(ReportSearchIntent.QueryChanged("pepe"))

        viewModel.onIntent(ReportSearchIntent.CriterionSelected(ReportSearchCriterion.CODE))
        advanceUntilIdle()

        assertEquals(ReportSearchCriterion.CODE to "", reports.lastSearch)
    }
}
