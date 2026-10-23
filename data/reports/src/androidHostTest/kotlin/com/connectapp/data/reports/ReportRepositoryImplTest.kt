package com.connectapp.data.reports

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.connectapp.core.database.physio.PhysioDatabase
import com.connectapp.domain.reports.ReportError
import com.connectapp.domain.reports.ReportSearchCriterion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import java.util.Properties
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/** Clinical reports against the real PhysioDatabase schema (plain SQLite, foreign keys on). */
class ReportRepositoryImplTest {

    private val driver = JdbcSqliteDriver(
        JdbcSqliteDriver.IN_MEMORY,
        Properties().apply { put("foreign_keys", "true") },
    ).also { PhysioDatabase.Schema.create(it) }

    private val database = PhysioDatabase(driver)
    private var now = 1_700_000_000_000L
    private var nextRandom: ByteArray? = null
    private var draws = 0
    private val repository = ReportRepositoryImpl(
        database,
        now = { now },
        randomBytes = { size -> nextRandom?.also { nextRandom = null } ?: ByteArray(size) { (++draws + it).toByte() } },
        dispatcher = Dispatchers.Unconfined,
    )

    init {
        listOf("pro-ana" to "12345678Z", "pro-luis" to "87654321X").forEachIndexed { index, (id, nif) ->
            database.professionalQueries.insertProfessional(
                id, nif, "MADRID", "$index", "$index", id, "Prof", "$id prof", null, null, null, null, now, now,
            )
        }
        database.patientQueries.insertPatient(
            "pat-1", "NIF_NIE", "X1234567L", "X1234567L", "Íñigo", "López", "inigo lopez", null, null, null, null, now, now,
        )
        database.patientQueries.insertPatient(
            "pat-2", "PASSPORT", "PAA123456", "PAA123456", "Ana", "Gil", "ana gil", null, null, null, null, now, now,
        )
    }

    @AfterTest
    fun tearDown() = driver.close()

    private suspend fun create(patientId: String = "pat-1") =
        repository.create("pro-ana", patientId, "Datos", "Lumbalgia").getOrThrow()

    @Test
    fun `a new report gets a unique visible code and keeps stable references`() = runTest {
        val report = assertNotNull(repository.observe(create()).first())

        assertTrue(Regex("INF-[23456789A-HJ-NP-Z]{8}").matches(report.code))
        assertEquals("pat-1", report.patient.id)
        assertEquals("Íñigo", report.patient.firstName)
        assertEquals("pro-ana", report.professional.id)
        assertEquals(0, report.versionCount)
    }

    @Test
    fun `a code collision draws a new code`() = runTest {
        nextRandom = ByteArray(8)
        val first = assertNotNull(repository.observe(create()).first()).code
        nextRandom = ByteArray(8)

        val second = assertNotNull(repository.observe(create()).first()).code

        assertNotEquals(first, second)
    }

    @Test
    fun `an archived patient cannot get new reports`() = runTest {
        database.patientQueries.setArchivedAt(now, now, "pat-2")

        assertEquals(
            ReportError.PatientNotAvailable,
            repository.create("pro-ana", "pat-2", "Datos", "Dolencia").exceptionOrNull(),
        )
    }

    // D17: only the author edits and the replaced text is kept.
    @Test
    fun `editing keeps the previous version and only the author may do it`() = runTest {
        val id = create()
        now += 1_000

        assertEquals(ReportError.NotAuthor, repository.update(id, "pro-luis", "X", "Y").exceptionOrNull())
        repository.update(id, "pro-ana", "Datos corregidos", "Lumbalgia").getOrThrow()
        repository.update(id, "pro-ana", "Datos corregidos", "Lumbalgia").getOrThrow() // no change, no version

        val report = assertNotNull(repository.observe(id).first())
        assertEquals("Datos corregidos", report.reportText)
        assertEquals(1, report.versionCount)
        val version = repository.observeVersions(id).first().single()
        assertEquals("Datos", version.reportText)
        assertEquals(now, version.replacedAtEpochMillis)
    }

    @Test
    fun `an annulled report keeps its reason and can no longer be edited`() = runTest {
        val id = create()

        assertEquals(ReportError.NotAuthor, repository.annul(id, "pro-luis", "Error").exceptionOrNull())
        repository.annul(id, "pro-ana", "Paciente equivocado").getOrThrow()

        val report = assertNotNull(repository.observe(id).first())
        assertEquals("Paciente equivocado", report.annulment?.reason)
        assertEquals(ReportError.Annulled, repository.update(id, "pro-ana", "X", "Y").exceptionOrNull())
        assertEquals(ReportError.Annulled, repository.annul(id, "pro-ana", "Otra vez").exceptionOrNull())
    }

    @Test
    fun `search by code, patient name or patient document lists annulled reports too`() = runTest {
        val inigo = create("pat-1")
        val ana = create("pat-2")
        repository.annul(ana, "pro-ana", "Duplicado").getOrThrow()
        val inigoCode = assertNotNull(repository.observe(inigo).first()).code

        suspend fun search(criterion: ReportSearchCriterion, query: String) =
            repository.search(criterion, query).first()

        assertEquals(listOf(inigo), search(ReportSearchCriterion.CODE, inigoCode.lowercase()).map { it.id })
        assertEquals(listOf(inigo), search(ReportSearchCriterion.PATIENT_NAME, "LOPEZ").map { it.id })
        val byDocument = search(ReportSearchCriterion.PATIENT_DOCUMENT, "paa 123")
        assertEquals(listOf(ana), byDocument.map { it.id })
        assertTrue(byDocument.single().isAnnulled)
    }

    @Test
    fun `the patient picker suggests active patients by name or document`() = runTest {
        database.patientQueries.setArchivedAt(now, now, "pat-2")

        assertEquals(listOf("pat-1"), repository.suggestPatients("inigo").map { it.id })
        assertEquals(listOf("pat-1"), repository.suggestPatients("x123").map { it.id })
        assertTrue(repository.suggestPatients("ana").isEmpty())
    }

    @Test
    fun `reports can never be deleted while the schema protects them`() = runTest {
        val id = create()
        repository.update(id, "pro-ana", "Nuevo", "Lumbalgia").getOrThrow()

        assertFailsWith<Exception> { database.patientQueries.deletePatientById("pat-1") }
        assertFailsWith<Exception> {
            database.clinicalReportQueries.annulReport(now, null, now, id) // reason is mandatory
        }
    }
}
