package com.connectapp.domain.reports

import com.connectapp.domain.model.ProfessionalProfile
import com.connectapp.domain.reports.fake.FakeAuthRepository
import com.connectapp.domain.shared.patient.PatientOption
import com.connectapp.domain.shared.professional.ProfessionalCollege
import com.connectapp.domain.usecase.CurrentAuthorUseCase
import com.connectapp.domain.usecase.ObserveCurrentProfessionalUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ReportUseCasesTest {

    private val auth = FakeAuthRepository()
    private val reports = RecordingRepository()
    private val author = CurrentAuthorUseCase(ObserveCurrentProfessionalUseCase(auth))

    private fun signIn() {
        auth.professional.value = ProfessionalProfile("pro-ana", "12345678Z", ProfessionalCollege.MADRID, "1", "Ana", "Gil")
    }

    @Test
    fun `the author of a new report is the professional of the session`() = runTest {
        signIn()

        CreateReportUseCase(reports, author)("pat-1", " Datos ", "Lumbalgia")

        assertEquals(listOf("pro-ana", "pat-1", "Datos", "Lumbalgia"), reports.lastCreate)
    }

    @Test
    fun `without a professional in session nothing is written`() = runTest {
        val result = CreateReportUseCase(reports, author)("pat-1", "Datos", "Lumbalgia")

        assertEquals(ReportError.NotAuthenticated, result.exceptionOrNull())
        assertNull(reports.lastCreate)
    }

    @Test
    fun `a patient and both texts are required`() = runTest {
        signIn()
        val create = CreateReportUseCase(reports, author)

        assertEquals(ReportError.InvalidInput, create(null, "Datos", "Lumbalgia").exceptionOrNull())
        assertEquals(ReportError.InvalidInput, create("pat-1", " ", "Lumbalgia").exceptionOrNull())
        assertEquals(ReportError.InvalidInput, create("pat-1", "Datos", "").exceptionOrNull())
        assertNull(reports.lastCreate)
    }

    // D17: annulling needs a reason.
    @Test
    fun `annulling needs a reason and is done as the session author`() = runTest {
        signIn()
        val annul = AnnulReportUseCase(reports, author)

        assertEquals(ReportError.InvalidInput, annul("rep-1", "  ").exceptionOrNull())
        annul("rep-1", " Paciente equivocado ")

        assertEquals(listOf("rep-1", "pro-ana", "Paciente equivocado"), reports.lastAnnul)
    }

    private class RecordingRepository : ReportRepository {
        var lastCreate: List<String>? = null
        var lastAnnul: List<String>? = null

        override suspend fun create(authorId: String, patientId: String, reportText: String, conditionText: String): Result<String> {
            lastCreate = listOf(authorId, patientId, reportText, conditionText)
            return Result.success("rep-1")
        }

        override suspend fun update(id: String, authorId: String, reportText: String, conditionText: String) =
            Result.success(Unit)

        override suspend fun annul(id: String, authorId: String, reason: String): Result<Unit> {
            lastAnnul = listOf(id, authorId, reason)
            return Result.success(Unit)
        }

        override fun observe(id: String): Flow<ClinicalReport?> = emptyFlow()
        override fun observeVersions(id: String): Flow<List<ReportVersion>> = emptyFlow()
        override fun search(criterion: ReportSearchCriterion, query: String): Flow<List<ReportSummary>> = emptyFlow()
        override suspend fun suggestPatients(query: String): List<PatientOption> = emptyList()
    }
}
