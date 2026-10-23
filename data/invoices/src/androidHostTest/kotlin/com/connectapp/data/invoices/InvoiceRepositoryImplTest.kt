package com.connectapp.data.invoices

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.connectapp.core.database.physio.PhysioDatabase
import com.connectapp.domain.invoices.InvoiceError
import com.connectapp.domain.invoices.InvoiceSearchCriterion
import com.connectapp.domain.invoices.NewInvoice
import com.connectapp.domain.invoices.PaymentMethod
import com.connectapp.domain.shared.money.Money
import com.connectapp.domain.shared.professional.ProfessionalCollege
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import java.util.Properties
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Payment receipts against the real PhysioDatabase schema (plain SQLite, foreign keys on). */
class InvoiceRepositoryImplTest {

    private val driver = JdbcSqliteDriver(
        JdbcSqliteDriver.IN_MEMORY,
        Properties().apply { put("foreign_keys", "true") },
    ).also { PhysioDatabase.Schema.create(it) }

    private val database = PhysioDatabase(driver)
    private var now = 1_700_000_000_000L
    private var year = "2026"
    private val repository = InvoiceRepositoryImpl(
        database,
        now = { now },
        seriesOf = { year },
        dispatcher = Dispatchers.Unconfined,
    )

    init {
        listOf("pro-ana" to "12345678Z", "pro-luis" to "87654321X").forEachIndexed { index, (id, nif) ->
            database.professionalQueries.insertProfessional(
                id, nif, "MADRID", "COL-$index", "COL-$index", "Ana", "Gil", "ana gil", null, null, null, null, now, now,
            )
        }
        database.patientQueries.insertPatient(
            "pat-1", "NIF_NIE", "X1234567L", "X1234567L", "Íñigo", "López", "inigo lopez", null, null, null, null, now, now,
        )
        database.patientQueries.insertPatient(
            "pat-2", "PASSPORT", "PAA123456", "PAA123456", "Eva", "Sanz", "eva sanz", null, null, null, null, now, now,
        )
    }

    @AfterTest
    fun tearDown() = driver.close()

    private suspend fun create(
        patientId: String = "pat-1",
        author: String = "pro-ana",
        paid: Boolean = false,
        method: PaymentMethod? = null,
    ) = repository.create(author, NewInvoice(patientId, "Sesión de fisioterapia", Money(4550), paid, method))

    // D6: sequential and gapless within the series of each year.
    @Test
    fun `numbers are sequential within each yearly series`() = runTest {
        val first = create().getOrThrow()
        val second = create().getOrThrow()
        repository.void(second, "pro-ana", "Duplicado").getOrThrow()
        val third = create(author = "pro-luis").getOrThrow()
        year = "2027"
        val nextYear = create().getOrThrow()

        val numbers = listOf(first, second, third, nextYear).map { assertNotNull(repository.observe(it).first()).number }
        assertEquals(listOf("2026-000001", "2026-000002", "2026-000003", "2027-000001"), numbers)
    }

    @Test
    fun `a failed issue does not use up a number`() = runTest {
        database.patientQueries.setArchivedAt(now, now, "pat-2")

        assertEquals(InvoiceError.PatientNotAvailable, create("pat-2").exceptionOrNull())
        val id = create().getOrThrow()

        assertEquals("2026-000001", assertNotNull(repository.observe(id).first()).number)
    }

    @Test
    fun `issuer and patient are copied onto the receipt`() = runTest {
        val id = create(paid = true, method = PaymentMethod.BIZUM).getOrThrow()
        database.patientQueries.updatePatient(
            "NIF_NIE", "X1234567L", "X1234567L", "Iñaki", "López", "inaki lopez", null, null, null, null, now, "pat-1",
        )

        val invoice = assertNotNull(repository.observe(id).first())
        assertEquals("Íñigo", invoice.recipient.firstName)
        assertEquals("12345678Z", invoice.issuer.nif)
        assertEquals(ProfessionalCollege.MADRID, invoice.issuer.college)
        assertEquals(Money(4550), invoice.amount)
        assertTrue(invoice.paid)
        assertEquals(PaymentMethod.BIZUM, invoice.paymentMethod)
        assertEquals(now, invoice.issuedAtEpochMillis)
    }

    // D5: only the author changes the collection status or voids.
    @Test
    fun `only the author marks as paid or voids and a voided receipt is frozen`() = runTest {
        val id = create().getOrThrow()

        assertEquals(InvoiceError.NotAuthor, repository.setPaid(id, "pro-luis", true, null).exceptionOrNull())
        repository.setPaid(id, "pro-ana", true, PaymentMethod.CASH).getOrThrow()
        assertEquals(PaymentMethod.CASH, assertNotNull(repository.observe(id).first()).paymentMethod)
        repository.setPaid(id, "pro-ana", false, PaymentMethod.CASH).getOrThrow()
        assertNull(assertNotNull(repository.observe(id).first()).paymentMethod)

        assertEquals(InvoiceError.NotAuthor, repository.void(id, "pro-luis", "Error").exceptionOrNull())
        repository.void(id, "pro-ana", "Importe equivocado").getOrThrow()
        val voided = assertNotNull(repository.observe(id).first())
        assertEquals("Importe equivocado", voided.voiding?.reason)
        assertFalse(voided.paid)
        assertEquals(InvoiceError.Voided, repository.setPaid(id, "pro-ana", true, null).exceptionOrNull())
        assertEquals(InvoiceError.Voided, repository.void(id, "pro-ana", "Otra vez").exceptionOrNull())
    }

    @Test
    fun `search by number patient name or document lists voided receipts too`() = runTest {
        val inigo = create("pat-1").getOrThrow()
        val eva = create("pat-2").getOrThrow()
        repository.void(eva, "pro-ana", "Duplicado").getOrThrow()

        suspend fun search(criterion: InvoiceSearchCriterion, query: String) =
            repository.search(criterion, query).first().map { it.id }

        assertEquals(listOf(inigo), search(InvoiceSearchCriterion.NUMBER, "000001"))
        assertEquals(listOf(eva, inigo), search(InvoiceSearchCriterion.NUMBER, "2026-"))
        assertEquals(listOf(inigo), search(InvoiceSearchCriterion.PATIENT_NAME, "LOPEZ"))
        assertEquals(listOf(eva), search(InvoiceSearchCriterion.PATIENT_DOCUMENT, "paa 123"))
        assertTrue(repository.search(InvoiceSearchCriterion.NUMBER, "000002").first().single().isVoided)
        assertTrue(search(InvoiceSearchCriterion.NUMBER, "%").isEmpty())
    }

    @Test
    fun `receipts and their patients can never be deleted`() = runTest {
        create().getOrThrow()

        assertFailsWith<Exception> { database.patientQueries.deletePatientById("pat-1") }
        assertFailsWith<Exception> { database.professionalQueries.deleteProfessionalById("pro-ana") }
    }

    @Test
    fun `the display number pads to six digits`() {
        assertEquals("2026-000042", displayNumber("2026", 42))
        assertEquals("2026-1234567", displayNumber("2026", 1_234_567))
    }
}
