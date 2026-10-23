package com.connectapp.core.database

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.connectapp.core.database.physio.PhysioDatabase
import java.util.Properties
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Data-model rules of PhysioDatabase v1 (docs/00-auditoria-y-plan.md §6): uniqueness, referential
 * integrity, logical deletion and exact amounts. Runs on plain SQLite through JDBC with foreign
 * keys enabled, exactly as the encrypted drivers configure every connection.
 */
class PhysioDatabaseSchemaTest {

    private val driver = JdbcSqliteDriver(
        JdbcSqliteDriver.IN_MEMORY,
        Properties().apply { put("foreign_keys", "true") },
    ).also { PhysioDatabase.Schema.create(it) }

    private val db = PhysioDatabase(driver)

    @AfterTest
    fun tearDown() = driver.close()

    private fun professional(
        id: String = "pro-1",
        nif: String = "12345678Z",
        license: String = "COL-1",
        college: String = "MADRID",
    ) = db.professionalQueries.insertProfessional(
        id, nif, college, license, license, "Aina", "García", "aina garcia", null, null, null, null, NOW, NOW,
    )

    private fun patient(id: String = "pat-1", nif: String = "X1234567L", type: String = "NIF_NIE") =
        db.patientQueries.insertPatient(
            id, type, nif, nif, "Iñigo", "López", "inigo lopez", null, null, null, null, NOW, NOW,
        )

    private fun account(id: String = "acc-1", email: String = "aina@clinica.es", professionalId: String? = null) =
        db.accountQueries.insertAccount(id, "Aina", "García", email, "pbkdf2_sha256\$1\$c2FsdA==\$aGFzaA==", null, professionalId, NOW, NOW)

    private fun report(id: String = "rep-1", code: String = "ABCD234567", professionalId: String = "pro-1", patientId: String = "pat-1") =
        db.clinicalReportQueries.insertReport(id, code, professionalId, patientId, "Datos", "Dolencia", NOW, NOW)

    private fun invoice(
        id: String = "inv-1",
        number: Long = 1,
        display: String = "2026-000001",
        amountCents: Long = 4550,
        paid: Long = 0,
        paymentMethod: String? = null,
    ) = db.invoiceQueries.insertInvoice(
        id, "2026", number, display, "pro-1", "pat-1",
        "Aina", "García", "12345678Z", "MADRID", "COL-1",
        "Iñigo", "López", "NIF_NIE", "X1234567L",
        "Sesión de fisioterapia", amountCents, "EUR", paid, paymentMethod, NOW, NOW, NOW,
    )

    // Guards every "cannot reference/delete" assertion below against passing for the wrong reason.
    @Test
    fun `foreign keys are enforced on the test connection`() {
        val enabled = driver.executeQuery(
            null,
            "PRAGMA foreign_keys",
            { cursor -> app.cash.sqldelight.db.QueryResult.Value(if (cursor.next().value) cursor.getLong(0) else null) },
            0,
        ).value
        assertEquals(1L, enabled)
    }

    @Test
    fun `schema version is 1`() {
        assertEquals(1L, PhysioDatabase.Schema.version)
    }

    @Test
    fun `account email is unique once normalized`() {
        account()
        assertFailsWith<Exception> { account(id = "acc-2", email = "aina@clinica.es") }
        assertEquals(1L, db.accountQueries.countAccounts().executeAsOne())
    }

    @Test
    fun `an account can link to at most one professional and each professional to one account`() {
        professional()
        account(professionalId = "pro-1")
        assertFailsWith<Exception> { account(id = "acc-2", email = "otra@clinica.es", professionalId = "pro-1") }
        assertFailsWith<Exception> { account(id = "acc-3", email = "x@clinica.es", professionalId = "missing") }
        assertEquals("pro-1", db.accountQueries.selectAccountByEmail("aina@clinica.es").executeAsOne().professional_id)
    }

    @Test
    fun `professional NIF is unique and the license number is unique within its college`() {
        professional()
        assertFailsWith<Exception> { professional(id = "pro-2", nif = "12345678Z", license = "COL-2") }
        assertFailsWith<Exception> { professional(id = "pro-3", nif = "87654321X", license = "COL-1") }
        // D9: each college numbers its members independently, so the same number elsewhere is fine.
        professional(id = "pro-4", nif = "87654321X", license = "COL-1", college = "GALICIA")
    }

    @Test
    fun `patient document is unique per document type`() {
        patient()
        assertFailsWith<Exception> { patient(id = "pat-2", nif = "X1234567L") }
        patient(id = "pat-3", nif = "X1234567L", type = "PASSPORT")
    }

    @Test
    fun `a report must reference an existing professional and patient`() {
        professional()
        patient()
        assertFailsWith<Exception> { report(patientId = "missing") }
        assertFailsWith<Exception> { report(professionalId = "missing") }
        report()
        assertNotNull(db.clinicalReportQueries.selectReportByCode("ABCD234567").executeAsOneOrNull())
    }

    @Test
    fun `report codes are unique`() {
        professional()
        patient()
        report()
        assertFailsWith<Exception> { report(id = "rep-2", code = "ABCD234567") }
    }

    // D8: referenced professionals and patients can only be archived, never physically deleted.
    @Test
    fun `professionals and patients referenced by a report cannot be deleted`() {
        professional()
        patient()
        report()
        assertFailsWith<Exception> { db.professionalQueries.deleteProfessionalById("pro-1") }
        assertFailsWith<Exception> { db.patientQueries.deletePatientById("pat-1") }
    }

    @Test
    fun `deleting an unreferenced patient also deletes its signatures`() {
        patient()
        db.patientSignatureQueries.insertSignature("sig-1", "pat-1", byteArrayOf(1, 2, 3), "PATIENT", null, null, "v1", NOW)

        db.patientQueries.deletePatientById("pat-1")

        assertNull(db.patientSignatureQueries.selectLatestSignature("pat-1").executeAsOneOrNull())
    }

    // D13: re-signing keeps the earlier signatures; a representative must be identified.
    @Test
    fun `signatures are kept as history and a representative needs name and document`() {
        patient()
        db.patientSignatureQueries.insertSignature("sig-1", "pat-1", byteArrayOf(1), "PATIENT", null, null, "v1", NOW)
        db.patientSignatureQueries.insertSignature(
            "sig-2", "pat-1", byteArrayOf(2), "REPRESENTATIVE", "Rosa López", "12345678Z", "v2", NOW + 1,
        )

        assertEquals(2L, db.patientSignatureQueries.countSignatures("pat-1").executeAsOne())
        assertEquals("sig-2", db.patientSignatureQueries.selectLatestSignature("pat-1").executeAsOne().id)
        assertFailsWith<Exception> {
            db.patientSignatureQueries.insertSignature("sig-3", "pat-1", byteArrayOf(3), "REPRESENTATIVE", null, null, "v2", NOW)
        }
    }

    // D17: an annulment always has its reason, and versions keep the report from being deleted.
    @Test
    fun `report annulment needs a reason and versions point at an existing report`() {
        professional()
        patient()
        report()
        assertFailsWith<Exception> { db.clinicalReportQueries.annulReport(NOW, null, NOW, "rep-1") }
        db.clinicalReportQueries.annulReport(NOW, "Error", NOW, "rep-1")
        assertFailsWith<Exception> { db.clinicalReportQueries.insertVersion("v-1", "missing", "a", "b", NOW, NOW) }
    }

    @Test
    fun `invoice numbers are sequential and unique within a series`() {
        professional()
        patient()
        db.invoiceQueries.insertSeriesIfMissing("2026", "2026")
        assertEquals(1L, db.invoiceQueries.selectSeriesNextNumber("2026").executeAsOne())

        db.invoiceQueries.transaction {
            val number = db.invoiceQueries.selectSeriesNextNumber("2026").executeAsOne()
            invoice(number = number)
            db.invoiceQueries.incrementSeriesNumber("2026")
        }

        assertEquals(2L, db.invoiceQueries.selectSeriesNextNumber("2026").executeAsOne())
        assertFailsWith<Exception> { invoice(id = "inv-2", number = 1, display = "2026-000001-dup") }
        assertFailsWith<Exception> { invoice(id = "inv-3", number = 2, display = "2026-000001") }
    }

    @Test
    fun `receipts need a positive amount and are voided with a reason instead of deleted`() {
        professional()
        patient()
        db.invoiceQueries.insertSeriesIfMissing("2026", "2026")
        assertFailsWith<Exception> { invoice(amountCents = -1) }
        assertFailsWith<Exception> { invoice(amountCents = 0) }
        // A payment method only makes sense for a paid receipt.
        assertFailsWith<Exception> { invoice(paid = 0, paymentMethod = "CASH") }
        invoice()
        assertFailsWith<Exception> { db.patientQueries.deletePatientById("pat-1") }
        // Voiding needs a reason (D6, D8).
        assertFailsWith<Exception> { db.invoiceQueries.voidInvoice(NOW, null, NOW, "inv-1") }
        db.invoiceQueries.voidInvoice(NOW, "Importe equivocado", NOW, "inv-1")
    }

    private companion object {
        const val NOW = 1_760_000_000_000L
    }
}
