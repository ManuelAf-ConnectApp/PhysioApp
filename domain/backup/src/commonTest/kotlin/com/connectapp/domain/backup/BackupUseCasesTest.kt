package com.connectapp.domain.backup

import com.connectapp.domain.backup.fake.FakeAuthRepository
import com.connectapp.domain.backup.fake.FakeBackupRepository
import com.connectapp.domain.model.AuthError
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BackupUseCasesTest {

    private val strong = "Copia-Clinica-2026!"
    private val repository = FakeBackupRepository()
    private val auth = FakeAuthRepository()

    @Test
    fun aBackupNeedsAPasswordThatFollowsThePolicyTypedTwice() = runTest {
        val create = CreateBackupUseCase(repository)

        assertEquals(BackupError.InvalidInput, create("corta", "corta").exceptionOrNull())
        assertEquals(BackupError.InvalidInput, create(strong, "$strong.").exceptionOrNull())
        assertEquals(0, repository.createCalls)
        assertTrue(create(strong, strong).isSuccess)
        assertEquals(1, repository.createCalls)
    }

    @Test
    fun theFormRulesNameEachWrongField() {
        assertEquals(
            setOf(BackupField.PASSWORD, BackupField.PASSWORD_CONFIRMATION),
            BackupValidation.createErrors("corta", ""),
        )
        assertEquals(
            setOf(BackupField.FILE, BackupField.PASSWORD, BackupField.SESSION_PASSWORD),
            BackupValidation.restoreErrors(hasFile = false, password = "", sessionPassword = ""),
        )
        // No session on the sign-in screen: nothing to type there.
        assertEquals(emptySet(), BackupValidation.restoreErrors(hasFile = true, password = "x", sessionPassword = null))
    }

    @Test
    fun theSignInScreenRestoresOnlyOnADeviceWithoutAccounts() = runTest {
        val restore = RestoreBackupUseCase(repository, auth)

        assertTrue(restore(byteArrayOf(1), strong, sessionPassword = null).isSuccess)

        assertEquals(true, repository.lastRestore?.third)
        assertTrue(auth.verifiedPasswords.isEmpty())
        assertEquals(0, auth.logoutCalls)
    }

    @Test
    fun settingsChecksTheSessionPasswordAndThenEndsTheSession() = runTest {
        val restore = RestoreBackupUseCase(repository, auth)

        assertTrue(restore(byteArrayOf(1), strong, sessionPassword = "Mi-Clave-Segura-1").isSuccess)

        assertEquals(listOf("Mi-Clave-Segura-1"), auth.verifiedPasswords)
        assertEquals(false, repository.lastRestore?.third)
        assertEquals(1, auth.logoutCalls)
    }

    @Test
    fun aWrongSessionPasswordRestoresNothing() = runTest {
        auth.verifyPasswordResult = Result.failure(AuthError.InvalidCredentials)
        val restore = RestoreBackupUseCase(repository, auth)

        assertEquals(AuthError.InvalidCredentials, restore(byteArrayOf(1), strong, "mal").exceptionOrNull())

        assertNull(repository.lastRestore)
        assertEquals(0, auth.logoutCalls)
    }

    @Test
    fun aFailedRestoreKeepsTheSession() = runTest {
        repository.restoreResult = Result.failure(BackupError.WrongPasswordOrFile)
        val restore = RestoreBackupUseCase(repository, auth)

        assertEquals(BackupError.WrongPasswordOrFile, restore(byteArrayOf(1), strong, "Mi-Clave").exceptionOrNull())
        assertEquals(0, auth.logoutCalls)
    }

    @Test
    fun aRestoreNeedsAFileAndItsPassword() = runTest {
        val restore = RestoreBackupUseCase(repository, auth)

        assertEquals(BackupError.InvalidInput, restore(null, strong, null).exceptionOrNull())
        assertEquals(BackupError.InvalidInput, restore(byteArrayOf(1), "", null).exceptionOrNull())
        assertNull(repository.lastRestore)
    }

    @Test
    fun homeRemindsWhenThereIsNoBackupOrItIsAWeekOld() = runTest {
        var now = 0L
        val reminder = ObserveBackupReminderUseCase(repository) { now }
        val day = 24 * 60 * 60 * 1000L

        assertEquals(BackupReminder.Due(null), reminder().first())

        now = 10 * day
        repository.markBackupSaved(4 * day)
        assertEquals(BackupReminder.NoReminder, reminder().first())

        now = 11 * day
        assertEquals(BackupReminder.Due(4 * day), reminder().first())
    }
}
