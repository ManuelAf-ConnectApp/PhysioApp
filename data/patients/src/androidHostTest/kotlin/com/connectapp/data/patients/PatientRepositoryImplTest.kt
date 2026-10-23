package com.connectapp.data.patients

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.connectapp.core.database.physio.PhysioDatabase
import com.connectapp.domain.patients.ConsentSignature
import com.connectapp.domain.patients.PatientDraft
import com.connectapp.domain.patients.PatientError
import com.connectapp.domain.patients.PatientSearchCriterion
import com.connectapp.domain.patients.SignatureSigner
import com.connectapp.domain.shared.validation.IdentityDocumentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import java.util.Properties
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Patient records and consents against the real PhysioDatabase schema (plain SQLite, FKs on). */
class PatientRepositoryImplTest {

    private val driver = JdbcSqliteDriver(
        JdbcSqliteDriver.IN_MEMORY,
        Properties().apply { put("foreign_keys", "true") },
    ).also { PhysioDatabase.Schema.create(it) }

    private val database = PhysioDatabase(driver)
    private var now = 1_700_000_000_000L
    private val repository = PatientRepositoryImpl(database, now = { now }, dispatcher = Dispatchers.Unconfined)

    private val inigo = PatientDraft(
        documentNumber = "x-1234567-l",
        firstName = "Íñigo",
        lastName = "López",
        email = " Inigo@Correo.ES ",
    )
    private val signedByPatient = ConsentSignature(byteArrayOf(1, 2, 3), SignatureSigner.PATIENT)

    @AfterTest
    fun tearDown() = driver.close()

    private suspend fun create(draft: PatientDraft = inigo) = repository.create(draft, signedByPatient).getOrThrow()

    @Test
    fun `a new patient is stored normalized with its first signature`() = runTest {
        val patient = assertNotNull(repository.observe(create()).first())

        assertEquals("X1234567L", patient.documentNumber)
        assertEquals("inigo@correo.es", patient.email)
        val signature = assertNotNull(patient.signature)
        assertEquals(SignatureSigner.PATIENT, signature.signer)
        assertEquals(1, signature.totalSignatures)
        assertContentEquals(byteArrayOf(1, 2, 3), repository.observeLatestSignatureImage(patient.id).first())
    }

    @Test
    fun `the same document is rejected within its type only`() = runTest {
        create()

        val sameNif = repository.create(inigo.copy(documentNumber = "X1234567L", firstName = "Otro"), signedByPatient)
        val asPassport = repository.create(inigo.copy(documentType = IdentityDocumentType.PASSPORT), signedByPatient)

        assertEquals(PatientError.DocumentAlreadyRegistered, sameNif.exceptionOrNull())
        assertTrue(asPassport.isSuccess)
    }

    // D13: re-signing keeps the history and editing never touches the signature.
    @Test
    fun `re-signing adds a signature and editing keeps it`() = runTest {
        val id = create()
        now += 1_000

        repository.addSignature(
            id,
            ConsentSignature(byteArrayOf(9), SignatureSigner.REPRESENTATIVE, "Rosa López", "12345678Z"),
        ).getOrThrow()
        repository.update(id, inigo.copy(notes = "Lumbalgia")).getOrThrow()

        val signature = assertNotNull(repository.observe(id).first()?.signature)
        assertEquals(2, signature.totalSignatures)
        assertEquals(SignatureSigner.REPRESENTATIVE, signature.signer)
        assertEquals("Rosa López", signature.representativeName)
        assertContentEquals(byteArrayOf(9), repository.observeLatestSignatureImage(id).first())
        assertEquals("Lumbalgia", repository.observe(id).first()?.notes)
    }

    @Test
    fun `search uses one criterion at a time`() = runTest {
        create()
        create(PatientDraft(IdentityDocumentType.PASSPORT, "PAA123456", "Ana", "Gil", email = "ana@clinica.es"))

        suspend fun search(criterion: PatientSearchCriterion, query: String) =
            repository.search(criterion, query, includeArchived = false).first().map { it.firstName }

        assertEquals(listOf("Íñigo"), search(PatientSearchCriterion.DOCUMENT, "1234567l"))
        assertEquals(listOf("Ana"), search(PatientSearchCriterion.DOCUMENT, "paa 123"))
        assertEquals(listOf("Íñigo"), search(PatientSearchCriterion.FULL_NAME, "inigo LOPEZ"))
        assertEquals(listOf("Ana"), search(PatientSearchCriterion.EMAIL, "ANA@"))
        assertEquals(listOf("Ana", "Íñigo"), search(PatientSearchCriterion.EMAIL, ""))
    }

    @Test
    fun `archived patients leave the searches and can be restored`() = runTest {
        val id = create()

        repository.setArchived(id, true).getOrThrow()
        assertTrue(repository.search(PatientSearchCriterion.FULL_NAME, "", false).first().isEmpty())
        assertTrue(repository.search(PatientSearchCriterion.FULL_NAME, "", true).first().single().isArchived)

        repository.setArchived(id, false).getOrThrow()
        assertEquals(1, repository.search(PatientSearchCriterion.FULL_NAME, "", false).first().size)
    }

    @Test
    fun `a patient with reports can be archived but not deleted`() = runTest {
        val id = create()
        database.professionalQueries.insertProfessional(
            "pro-1", "12345678Z", "MADRID", "1", "1", "Ana", "Gil", "ana gil", null, null, null, null, now, now,
        )
        database.clinicalReportQueries.insertReport("rep-1", "INF-1", "pro-1", id, "texto", "estado", now, now)

        assertEquals(PatientError.HasClinicalRecords, repository.delete(id).exceptionOrNull())
        assertTrue(repository.setArchived(id, true).isSuccess)
    }

    @Test
    fun `deleting an unreferenced patient also removes its signatures`() = runTest {
        val id = create()

        repository.delete(id).getOrThrow()

        assertNull(repository.observe(id).first())
        assertNull(repository.observeLatestSignatureImage(id).first())
    }
}
