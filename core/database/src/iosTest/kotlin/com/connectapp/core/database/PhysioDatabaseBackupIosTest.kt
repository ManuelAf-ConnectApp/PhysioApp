package com.connectapp.core.database

import co.touchlab.sqliter.DatabaseFileContext
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Backups against the real SQLCipher on the iOS simulator (same SQL on Android): the copy is
 * encrypted with its own password, restores on a device with a different key, and a failed
 * restore changes nothing.
 */
class PhysioDatabaseBackupIosTest {

    private val name = "backup-ios-test.db"
    private val otherDeviceName = "backup-ios-test-other.db"
    private val passphrase = "a".repeat(32) + "0123456789abcdef0123456789abcdef"
    private val otherDevicePassphrase = "c".repeat(32) + "0123456789abcdef0123456789abcdef"
    private val password = "Copia-Clinica-2026!"

    @BeforeTest
    fun setUp() {
        DatabaseFileContext.deleteDatabase(name)
        DatabaseFileContext.deleteDatabase(otherDeviceName)
    }

    @AfterTest
    fun tearDown() = setUp()

    private fun insertPatient(db: EncryptedPhysioDatabase, id: String, notes: String) =
        db.database.patientQueries.insertPatient(
            id, "NIF_NIE", "X1234567L-$id", "X1234567L-$id", "Iñigo", "López", "inigo lopez", null, null, null, notes, 1L, 1L,
        )

    private fun insertAccount(db: EncryptedPhysioDatabase) =
        db.database.accountQueries.insertAccount("acc-1", "Ana", "Ruiz", "ana@example.com", "hash", null, null, 1L, 1L)

    private fun notesOf(db: EncryptedPhysioDatabase, id: String): String? =
        db.database.patientQueries.selectPatientById(id).executeAsOneOrNull()?.notes

    @Test
    fun aRestoreBringsBackExactlyTheDataOfTheBackup() {
        openPhysioDatabase(passphrase, name).use { db ->
            insertPatient(db, "pat-1", "dato clinico")
            insertAccount(db)
            val backup = db.exportBackup(password, createdAt = 42L)
            insertPatient(db, "pat-2", "posterior a la copia")

            val info = db.restoreBackup(backup, password, requireNoAccounts = false)

            assertEquals(42L, info.createdAt)
            assertEquals("dato clinico", notesOf(db, "pat-1"))
            assertNull(notesOf(db, "pat-2"))
            assertEquals(1L, db.database.accountQueries.countAccounts().executeAsOne())
        }
    }

    @Test
    fun aBackupRestoresOnAnotherDeviceWithItsOwnKey() {
        val backup = openPhysioDatabase(passphrase, name).use { db ->
            insertPatient(db, "pat-1", "dato clinico")
            insertAccount(db)
            db.exportBackup(password, createdAt = 1L)
        }

        openPhysioDatabase(otherDevicePassphrase, otherDeviceName).use { db ->
            db.restoreBackup(backup, password, requireNoAccounts = true)
            assertEquals("dato clinico", notesOf(db, "pat-1"))
        }
        // The restored database still opens with that device's key.
        openPhysioDatabase(otherDevicePassphrase, otherDeviceName).use { db ->
            assertEquals(1L, db.database.accountQueries.countAccounts().executeAsOne())
        }
    }

    @Test
    fun theBackupIsEncryptedAndHasNoClearText() {
        val backup = openPhysioDatabase(passphrase, name).use { db ->
            insertPatient(db, "pat-1", "dato clinico")
            db.exportBackup(password, createdAt = 1L)
        }

        val plainHeader = "SQLite format 3".encodeToByteArray()
        assertTrue(backup.size > plainHeader.size)
        assertFalse(backup.copyOfRange(0, plainHeader.size).contentEquals(plainHeader), "plain SQLite header found")
        assertFalse(backup.decodeToString().contains("dato clinico"), "clear text found in the backup")
    }

    @Test
    fun aWrongPasswordIsRejectedAndChangesNothing() {
        openPhysioDatabase(passphrase, name).use { db ->
            insertPatient(db, "pat-1", "dato clinico")
            val backup = db.exportBackup(password, createdAt = 1L)
            insertPatient(db, "pat-2", "posterior a la copia")

            assertFailsWith<PhysioBackupException.InvalidFileOrPassword> {
                db.restoreBackup(backup, "Otra-Contrasena-99!", requireNoAccounts = false)
            }
            assertEquals("posterior a la copia", notesOf(db, "pat-2"))
        }
    }

    @Test
    fun aFileThatIsNotABackupIsRejected() {
        openPhysioDatabase(passphrase, name).use { db ->
            assertFailsWith<PhysioBackupException.InvalidFileOrPassword> {
                db.restoreBackup("not a backup".encodeToByteArray(), password, requireNoAccounts = false)
            }
            assertFailsWith<PhysioBackupException.InvalidFileOrPassword> {
                db.restoreBackup(ByteArray(0), password, requireNoAccounts = false)
            }
        }
    }

    @Test
    fun theSignInScreenCannotRestoreOverExistingAccounts() {
        openPhysioDatabase(passphrase, name).use { db ->
            insertAccount(db)
            val backup = db.exportBackup(password, createdAt = 1L)

            assertFailsWith<PhysioBackupException.DeviceHasAccounts> {
                db.restoreBackup(backup, password, requireNoAccounts = true)
            }
        }
    }

    @Test
    fun theDatabaseKeepsWorkingAfterABackupAndARestore() {
        openPhysioDatabase(passphrase, name).use { db ->
            insertPatient(db, "pat-1", "dato clinico")
            val backup = db.exportBackup(password, createdAt = 1L)
            db.restoreBackup(backup, password, requireNoAccounts = false)

            insertPatient(db, "pat-2", "despues")
            assertEquals("despues", notesOf(db, "pat-2"))
            // Foreign keys are enforced again on the app's connections.
            assertFailsWith<Throwable> {
                db.database.clinicalReportQueries.insertReport("r", "CODE234567", "missing", "missing", "d", "c", 1L, 1L)
            }
        }
    }
}
