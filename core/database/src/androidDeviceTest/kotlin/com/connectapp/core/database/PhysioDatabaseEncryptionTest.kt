package com.connectapp.core.database

import android.database.sqlite.SQLiteDatabase
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Runs on a real Android runtime: proves the database file is encrypted at rest with SQLCipher
 * (docs/00-auditoria-y-plan.md D7), not merely that the code compiles.
 */
class PhysioDatabaseEncryptionTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val name = "encryption-test.db"
    private val passphrase = "a".repeat(32) + "0123456789abcdef0123456789abcdef"
    private val otherPassphrase = "b".repeat(32) + "0123456789abcdef0123456789abcdef"

    @Before
    fun setUp() {
        context.deleteDatabase(name)
    }

    @After
    fun tearDown() {
        context.deleteDatabase(name)
    }

    private fun insertPatient(db: EncryptedPhysioDatabase) =
        db.database.patientQueries.insertPatient(
            "pat-1", "NIF_NIE", "X1234567L", "X1234567L", "Iñigo", "López", "inigo lopez", null, null, null, "dato clínico", 1L, 1L,
        )

    @Test
    fun runtimeIsSqlCipher() {
        openPhysioDatabase(passphrase, name).use { db ->
            assertTrue(db.cipherVersion.isNotBlank(), "PRAGMA cipher_version must report SQLCipher")
        }
    }

    @Test
    fun fileOnDiskIsNotPlainSqliteAndContainsNoClearText() {
        openPhysioDatabase(passphrase, name).use(::insertPatient)

        val bytes = context.getDatabasePath(name).readBytes()
        val plainHeader = "SQLite format 3\u0000".encodeToByteArray()
        assertFalse(bytes.copyOfRange(0, plainHeader.size).contentEquals(plainHeader), "plain SQLite header found")
        assertFalse(String(bytes, Charsets.ISO_8859_1).contains("dato clínico"), "clear text found on disk")
        assertFalse(String(bytes, Charsets.ISO_8859_1).contains("X1234567L"), "clear NIF found on disk")
    }

    @Test
    fun dataSurvivesReopeningWithTheSamePassphrase() {
        openPhysioDatabase(passphrase, name).use(::insertPatient)

        openPhysioDatabase(passphrase, name).use { db ->
            val patient = db.database.patientQueries.selectPatientById("pat-1").executeAsOne()
            assertEquals("dato clínico", patient.notes)
        }
    }

    @Test
    fun aWrongPassphraseCannotOpenTheDatabase() {
        openPhysioDatabase(passphrase, name).use(::insertPatient)

        assertFailsWith<Exception> { openPhysioDatabase(otherPassphrase, name).close() }
        assertTrue(context.getDatabasePath(name).exists(), "a failed open must never delete the file")
    }

    @Test
    fun thePlatformSqliteCannotReadTheFile() {
        openPhysioDatabase(passphrase, name).use(::insertPatient)

        assertFailsWith<Exception> {
            SQLiteDatabase.openDatabase(context.getDatabasePath(name).path, null, SQLiteDatabase.OPEN_READONLY).use {
                it.rawQuery("SELECT count(*) FROM patient", null).use { cursor -> cursor.moveToFirst() }
            }
        }
    }

    @Test
    fun foreignKeysAreEnforcedOnEncryptedConnections() {
        openPhysioDatabase(passphrase, name).use { db ->
            assertFailsWith<Exception> {
                db.database.clinicalReportQueries.insertReport("r", "CODE234567", "missing", "missing", "d", "c", 1L, 1L)
            }
        }
    }

    @Test
    fun shortPassphrasesAreRejected() {
        assertFailsWith<IllegalArgumentException> { openPhysioDatabase("short", name) }
    }
}
