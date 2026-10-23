package com.connectapp.data.backup

import com.connectapp.core.database.PhysioBackupException
import com.connectapp.domain.backup.BackupError
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class BackupRepositoryImplTest {

    // 2026-10-21 15:30:00 UTC
    private val now = 1_792_596_600_000L

    private class FakeEngine : BackupEngine {
        var restoreFailure: Exception? = null
        var lastExport: Pair<String, Long>? = null

        override fun export(password: String, createdAt: Long): ByteArray {
            lastExport = password to createdAt
            return byteArrayOf(7)
        }

        override fun restore(bytes: ByteArray, password: String, requireNoAccounts: Boolean): Long {
            restoreFailure?.let { throw it }
            return 123L
        }
    }

    private class MemoryStore(var value: Long? = null) : LastBackupStore {
        override fun read(): Long? = value
        override fun write(epochMillis: Long) {
            value = epochMillis
        }
    }

    private val engine = FakeEngine()
    private val store = MemoryStore()

    private fun newRepository(dispatcher: TestDispatcher) = BackupRepositoryImpl(
        engine = engine,
        store = store,
        clock = { now },
        timeZone = { TimeZone.UTC },
        dispatcher = dispatcher,
    )

    @Test
    fun aBackupIsNamedByItsDateOnlyAndIsNotSavedUntilTheUserSavesIt() = runTest {
        val repository = newRepository(StandardTestDispatcher(testScheduler))

        val file = repository.createBackup("Copia-Clinica-2026!").getOrThrow()

        assertEquals("physioapp-copia-2026-10-21-1530.physiobackup", file.fileName)
        assertEquals(now, file.createdAt)
        assertEquals("Copia-Clinica-2026!" to now, engine.lastExport)
        assertNull(repository.observeLastBackupAt().first())

        repository.markBackupSaved(file.createdAt)
        assertEquals(now, repository.observeLastBackupAt().first())
        assertEquals(now, store.value)
    }

    @Test
    fun aRestoreCountsAsABackupWithTheBackupsOwnDate() = runTest {
        val repository = newRepository(StandardTestDispatcher(testScheduler))

        repository.restoreBackup(byteArrayOf(1), "x", requireNoAccounts = true).getOrThrow()

        assertEquals(123L, repository.observeLastBackupAt().first())
    }

    @Test
    fun databaseFailuresBecomeBackupErrors() = runTest {
        val repository = newRepository(StandardTestDispatcher(testScheduler))
        val cases = mapOf(
            PhysioBackupException.InvalidFileOrPassword() to BackupError.WrongPasswordOrFile,
            PhysioBackupException.IncompatibleVersion(madeWithNewerApp = true) to BackupError.IncompatibleVersion(true),
            PhysioBackupException.DeviceHasAccounts() to BackupError.DeviceHasAccounts,
        )
        cases.forEach { (failure, expected) ->
            engine.restoreFailure = failure
            assertEquals(expected, repository.restoreBackup(byteArrayOf(1), "x", false).exceptionOrNull())
        }
        engine.restoreFailure = IllegalStateException("disk full")
        assertIs<BackupError.Unknown>(repository.restoreBackup(byteArrayOf(1), "x", false).exceptionOrNull())
        assertNull(store.value)
    }

    @Test
    fun theLastBackupDateSurvivesARestart() = runTest {
        store.value = 99L
        val repository = newRepository(StandardTestDispatcher(testScheduler))

        assertEquals(99L, repository.observeLastBackupAt().first())
    }
}
