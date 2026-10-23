@file:OptIn(ExperimentalForeignApi::class)

package com.connectapp.core.database

import co.touchlab.sqliter.DatabaseFileContext
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.Foundation.NSURLIsExcludedFromBackupKey
import platform.Foundation.dataWithContentsOfFile
import platform.posix.memcpy
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Runs on the iOS simulator linked against the official SQLCipher.xcframework (see
 * fetchSqlCipherXcframework in build.gradle.kts): proves the iOS database is encrypted at rest.
 */
class PhysioDatabaseEncryptionIosTest {

    private val name = "encryption-ios-test.db"
    private val passphrase = "a".repeat(32) + "0123456789abcdef0123456789abcdef"
    private val otherPassphrase = "b".repeat(32) + "0123456789abcdef0123456789abcdef"

    @BeforeTest
    fun setUp() = DatabaseFileContext.deleteDatabase(name)

    @AfterTest
    fun tearDown() = DatabaseFileContext.deleteDatabase(name)

    private fun insertPatient(db: EncryptedPhysioDatabase) =
        db.database.patientQueries.insertPatient(
            "pat-1", "NIF_NIE", "X1234567L", "X1234567L", "Iñigo", "López", "inigo lopez", null, null, null, "dato clinico", 1L, 1L,
        )

    private fun fileBytes(): ByteArray {
        val data = checkNotNull(NSData.dataWithContentsOfFile(DatabaseFileContext.databasePath(name, null)))
        val bytes = ByteArray(data.length.toInt())
        if (bytes.isNotEmpty()) bytes.usePinned { memcpy(it.addressOf(0), data.bytes, data.length) }
        return bytes
    }

    @Test
    fun runtimeIsSqlCipher() {
        openPhysioDatabase(passphrase, name).use { db ->
            assertTrue(db.cipherVersion.isNotBlank(), "PRAGMA cipher_version must report SQLCipher")
        }
    }

    @Test
    fun fileOnDiskIsNotPlainSqliteAndContainsNoClearText() {
        openPhysioDatabase(passphrase, name).use(::insertPatient)

        val bytes = fileBytes()
        val plainHeader = "SQLite format 3".encodeToByteArray()
        assertTrue(bytes.size > plainHeader.size)
        assertFalse(bytes.copyOfRange(0, plainHeader.size).contentEquals(plainHeader), "plain SQLite header found")
        val text = bytes.decodeToString()
        assertFalse(text.contains("dato clinico"), "clear text found on disk")
        assertFalse(text.contains("X1234567L"), "clear NIF found on disk")
    }

    @Test
    fun dataSurvivesReopeningWithTheSamePassphrase() {
        openPhysioDatabase(passphrase, name).use(::insertPatient)

        openPhysioDatabase(passphrase, name).use { db ->
            assertEquals("dato clinico", db.database.patientQueries.selectPatientById("pat-1").executeAsOne().notes)
        }
    }

    @Test
    fun aWrongPassphraseCannotOpenTheDatabase() {
        openPhysioDatabase(passphrase, name).use(::insertPatient)

        assertFailsWith<Throwable> { openPhysioDatabase(otherPassphrase, name).close() }
    }

    @Test
    fun foreignKeysAreEnforcedOnEncryptedConnections() {
        openPhysioDatabase(passphrase, name).use { db ->
            assertFailsWith<Throwable> {
                db.database.clinicalReportQueries.insertReport("r", "CODE234567", "missing", "missing", "d", "c", 1L, 1L)
            }
        }
    }

    // A5 on iOS: no iCloud/computer backup of the database (its key never leaves this device).
    @Test
    fun theDatabaseFolderIsExcludedFromBackups() {
        openPhysioDatabase(passphrase, name).use(::insertPatient)

        val directory = DatabaseFileContext.databasePath(name, null).substringBeforeLast('/')
        val excluded = memScoped {
            val value = alloc<ObjCObjectVar<Any?>>()
            NSURL.fileURLWithPath(directory, isDirectory = true)
                .getResourceValue(value.ptr, forKey = NSURLIsExcludedFromBackupKey, error = null)
            value.value as? Boolean
        }
        assertEquals(true, excluded)
    }
}
