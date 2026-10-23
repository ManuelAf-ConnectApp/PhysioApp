package com.connectapp.core.database

import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull

/**
 * Backups on a real Android runtime with sqlcipher-android (the same checks run on the iOS
 * simulator in PhysioDatabaseBackupIosTest): own password, another device's key, nothing changes
 * on failure.
 */
class PhysioDatabaseBackupTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val name = "backup-test.db"
    private val otherDeviceName = "backup-test-other.db"
    private val passphrase = "a".repeat(32) + "0123456789abcdef0123456789abcdef"
    private val otherDevicePassphrase = "c".repeat(32) + "0123456789abcdef0123456789abcdef"
    private val password = "Copia-Clinica-2026!"

    @Before
    fun setUp() {
        context.deleteDatabase(name)
        context.deleteDatabase(otherDeviceName)
    }

    @After
    fun tearDown() = setUp()

    private fun insertPatient(db: EncryptedPhysioDatabase, id: String, notes: String) =
        db.database.patientQueries.insertPatient(
            id, "NIF_NIE", "X1234567L-$id", "X1234567L-$id", "Iñigo", "López", "inigo lopez", null, null, null, notes, 1L, 1L,
        )

    private fun notesOf(db: EncryptedPhysioDatabase, id: String): String? =
        db.database.patientQueries.selectPatientById(id).executeAsOneOrNull()?.notes

    @Test
    fun aRestoreBringsBackExactlyTheDataOfTheBackup() {
        openPhysioDatabase(passphrase, name).use { db ->
            insertPatient(db, "pat-1", "dato clínico")
            val backup = db.exportBackup(password, createdAt = 42L)
            insertPatient(db, "pat-2", "posterior a la copia")

            assertEquals(42L, db.restoreBackup(backup, password, requireNoAccounts = false).createdAt)
            assertEquals("dato clínico", notesOf(db, "pat-1"))
            assertNull(notesOf(db, "pat-2"))
        }
    }

    @Test
    fun aBackupRestoresOnAnotherDeviceAndHasNoClearText() {
        val backup = openPhysioDatabase(passphrase, name).use { db ->
            insertPatient(db, "pat-1", "dato clínico")
            db.exportBackup(password, createdAt = 1L)
        }
        assertFalse(String(backup, Charsets.ISO_8859_1).contains("X1234567L"), "clear NIF found in the backup")

        openPhysioDatabase(otherDevicePassphrase, otherDeviceName).use { db ->
            db.restoreBackup(backup, password, requireNoAccounts = true)
            assertEquals("dato clínico", notesOf(db, "pat-1"))
        }
    }

    @Test
    fun aWrongPasswordIsRejectedAndChangesNothing() {
        openPhysioDatabase(passphrase, name).use { db ->
            insertPatient(db, "pat-1", "dato clínico")
            val backup = db.exportBackup(password, createdAt = 1L)
            insertPatient(db, "pat-2", "posterior a la copia")

            assertFailsWith<PhysioBackupException.InvalidFileOrPassword> {
                db.restoreBackup(backup, "Otra-Contrasena-99!", requireNoAccounts = false)
            }
            assertEquals("posterior a la copia", notesOf(db, "pat-2"))
        }
    }
}
