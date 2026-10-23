package com.connectapp.data.professionals

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.connectapp.core.database.physio.PhysioDatabase
import com.connectapp.domain.professionals.ProfessionalDraft
import com.connectapp.domain.professionals.ProfessionalError
import com.connectapp.domain.professionals.ProfessionalSearchCriterion
import com.connectapp.domain.shared.professional.ProfessionalCollege
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import java.util.Properties
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Professional records against the real PhysioDatabase schema (plain SQLite, foreign keys on). */
class ProfessionalRepositoryImplTest {

    private val driver = JdbcSqliteDriver(
        JdbcSqliteDriver.IN_MEMORY,
        Properties().apply { put("foreign_keys", "true") },
    ).also { PhysioDatabase.Schema.create(it) }

    private val database = PhysioDatabase(driver)
    private var now = 1_700_000_000_000L
    private val repository = ProfessionalRepositoryImpl(database, now = { now }, dispatcher = Dispatchers.Unconfined)

    private val ana = ProfessionalDraft(
        nif = "12345678-z",
        college = ProfessionalCollege.MADRID,
        licenseNumber = " 28/01234 ",
        firstName = "Ana",
        lastName = "Gil Núñez",
        email = " Ana@Clinica.ES ",
        phone = " +34  600 123 456 ",
    )

    @AfterTest
    fun tearDown() = driver.close()

    private suspend fun create(draft: ProfessionalDraft = ana) = repository.create(draft).getOrThrow()

    @Test
    fun `a new record is stored normalized and keeps the licence as typed`() = runTest {
        val professional = assertNotNull(repository.observe(create()).first())

        assertEquals("12345678Z", professional.nif)
        assertEquals("28/01234", professional.licenseNumber)
        assertEquals("ana@clinica.es", professional.email)
        assertEquals("+34 600 123 456", professional.phone)
        assertNull(professional.address)
        assertFalse(professional.isArchived)
        assertTrue(professional.canDelete)
    }

    @Test
    fun `duplicate NIF or licence in the same college are rejected`() = runTest {
        create()

        val sameNif = repository.create(ana.copy(licenseNumber = "999"))
        val sameLicense = repository.create(ana.copy(nif = "X1234567L", licenseNumber = "28/01234"))
        val otherCollege = repository.create(ana.copy(nif = "X1234567L", college = ProfessionalCollege.GALICIA))

        assertEquals(ProfessionalError.NifAlreadyRegistered, sameNif.exceptionOrNull())
        assertEquals(ProfessionalError.LicenseNumberAlreadyRegistered, sameLicense.exceptionOrNull())
        assertTrue(otherCollege.isSuccess)
    }

    @Test
    fun `editing updates the same record without duplicating it`() = runTest {
        val id = create()

        repository.update(id, ana.copy(firstName = "Ana María", notes = "Turno de tarde")).getOrThrow()

        val all = repository.search(ProfessionalSearchCriterion.FULL_NAME, "", includeArchived = true).first()
        assertEquals(listOf(id), all.map { it.id })
        assertEquals("Ana María", all.single().firstName)
        assertEquals("Turno de tarde", repository.observe(id).first()?.notes)
    }

    @Test
    fun `editing cannot take another record's NIF`() = runTest {
        create()
        val other = create(ana.copy(nif = "X1234567L", licenseNumber = "28/5555"))

        val result = repository.update(other, ana.copy(licenseNumber = "28/5555"))

        assertEquals(ProfessionalError.NifAlreadyRegistered, result.exceptionOrNull())
    }

    @Test
    fun `search uses one criterion at a time and ignores accents and case`() = runTest {
        val ana = create()
        create(ProfessionalDraft("X1234567L", ProfessionalCollege.GALICIA, "15-777", "Íñigo", "López"))

        suspend fun search(criterion: ProfessionalSearchCriterion, query: String) =
            repository.search(criterion, query, includeArchived = false).first().map { it.firstName }

        assertEquals(listOf("Ana"), search(ProfessionalSearchCriterion.NIF, "12345"))
        assertEquals(listOf("Íñigo"), search(ProfessionalSearchCriterion.FULL_NAME, "inigo lop"))
        assertEquals(listOf("Ana"), search(ProfessionalSearchCriterion.FULL_NAME, "NUNEZ"))
        assertEquals(listOf("Íñigo"), search(ProfessionalSearchCriterion.LICENSE_NUMBER, "777"))
        assertEquals(emptyList(), search(ProfessionalSearchCriterion.FULL_NAME, "%"))
        assertEquals(listOf("Ana", "Íñigo"), search(ProfessionalSearchCriterion.NIF, ""))
        assertNotNull(ana)
    }

    // D8 (2026-10-20): "Borrar" archives; archived records leave the searches and can be restored.
    @Test
    fun `archived records only appear when asked for and can be restored`() = runTest {
        val id = create()

        repository.setArchived(id, archived = true).getOrThrow()
        assertTrue(repository.search(ProfessionalSearchCriterion.NIF, "", includeArchived = false).first().isEmpty())
        assertTrue(repository.search(ProfessionalSearchCriterion.NIF, "", includeArchived = true).first().single().isArchived)

        repository.setArchived(id, archived = false).getOrThrow()
        assertEquals(1, repository.search(ProfessionalSearchCriterion.NIF, "", includeArchived = false).first().size)
    }

    @Test
    fun `a record with an account can be neither archived nor deleted`() = runTest {
        val id = create()
        database.accountQueries.insertAccount("acc-1", "Ana", "Gil", "ana@clinica.es", "hash", null, id, now, now)

        assertEquals(ProfessionalError.HasAccount, repository.setArchived(id, true).exceptionOrNull())
        assertEquals(ProfessionalError.HasAccount, repository.delete(id).exceptionOrNull())
        assertFalse(assertNotNull(repository.observe(id).first()).canArchive)
    }

    @Test
    fun `a record with reports can be archived but not deleted`() = runTest {
        val id = create()
        database.patientQueries.insertPatient(
            "pat-1", "NIF_NIE", "X1234567L", "X1234567L", "Pepe", "Ruiz", "pepe ruiz", null, null, null, null, now, now,
        )
        database.clinicalReportQueries.insertReport("rep-1", "INF-1", id, "pat-1", "texto", "estado", now, now)

        assertEquals(ProfessionalError.HasClinicalRecords, repository.delete(id).exceptionOrNull())
        assertTrue(repository.setArchived(id, true).isSuccess)
    }

    @Test
    fun `a record without references is deleted for good`() = runTest {
        val id = create()

        repository.delete(id).getOrThrow()

        assertNull(repository.observe(id).first())
        assertEquals(ProfessionalError.NotFound, repository.delete(id).exceptionOrNull())
    }
}
