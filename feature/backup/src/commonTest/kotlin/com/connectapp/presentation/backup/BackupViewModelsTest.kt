package com.connectapp.presentation.backup

import com.connectapp.commonresources.backup_not_saved
import com.connectapp.commonresources.error_backup_device_has_accounts
import com.connectapp.commonresources.error_backup_newer
import com.connectapp.commonresources.error_backup_wrong_password
import com.connectapp.commonresources.error_unexpected
import com.connectapp.commonresources.error_wrong_password
import com.connectapp.commonresources.export.BINARY_MIME_TYPE
import com.connectapp.domain.backup.BackupError
import com.connectapp.domain.backup.CreateBackupUseCase
import com.connectapp.domain.backup.MarkBackupSavedUseCase
import com.connectapp.domain.backup.ObserveLastBackupUseCase
import com.connectapp.domain.backup.RestoreBackupUseCase
import com.connectapp.domain.model.AuthError
import com.connectapp.presentation.backup.create.CreateBackupEffect
import com.connectapp.presentation.backup.create.CreateBackupIntent
import com.connectapp.presentation.backup.create.CreateBackupViewModel
import com.connectapp.presentation.backup.fake.FakeAuthRepository
import com.connectapp.presentation.backup.fake.FakeBackupRepository
import com.connectapp.presentation.backup.restore.RestoreBackupEffect
import com.connectapp.presentation.backup.restore.RestoreBackupIntent
import com.connectapp.presentation.backup.restore.RestoreBackupViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class BackupViewModelsTest {

    private val dispatcher = StandardTestDispatcher()
    private val backups = FakeBackupRepository()
    private val auth = FakeAuthRepository()
    private val strong = "Copia-Clinica-2026!"

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = CreateBackupViewModel(
        CreateBackupUseCase(backups),
        MarkBackupSavedUseCase(backups),
        ObserveLastBackupUseCase(backups),
    )

    private fun restoreViewModel(fromSignIn: Boolean) =
        RestoreBackupViewModel(fromSignIn, RestoreBackupUseCase(backups, auth))

    private fun <E> TestScope.collectEffects(effects: kotlinx.coroutines.flow.Flow<E>, block: () -> Unit): List<E> {
        val collected = mutableListOf<E>()
        val job = launch { effects.toList(collected) }
        block()
        advanceUntilIdle()
        job.cancel()
        return collected
    }

    @Test
    fun `a backup is handed to the save picker and only counts once saved`() = runTest {
        val viewModel = createViewModel()
        viewModel.onIntent(CreateBackupIntent.PasswordChanged(strong))
        assertFalse(viewModel.state.value.canSubmit)
        viewModel.onIntent(CreateBackupIntent.ConfirmationChanged(strong))

        val effects = collectEffects(viewModel.effect) { viewModel.onIntent(CreateBackupIntent.CreateClicked) }

        val save = assertIs<CreateBackupEffect.SaveFile>(effects.single())
        assertEquals("physioapp.physiobackup", save.document.fileName)
        assertEquals(BINARY_MIME_TYPE, save.document.mimeType)
        assertNull(viewModel.state.value.lastBackupAt)

        viewModel.onIntent(CreateBackupIntent.SaveFinished(saved = true))
        advanceUntilIdle()

        assertEquals(10L, viewModel.state.value.lastBackupAt)
        assertTrue(viewModel.state.value.saved)
        assertEquals("", viewModel.state.value.password)
        assertEquals("", viewModel.state.value.confirmation)
    }

    @Test
    fun `a cancelled save is reported and does not count as a backup`() = runTest {
        val viewModel = createViewModel()
        viewModel.onIntent(CreateBackupIntent.PasswordChanged(strong))
        viewModel.onIntent(CreateBackupIntent.ConfirmationChanged(strong))
        collectEffects(viewModel.effect) { viewModel.onIntent(CreateBackupIntent.CreateClicked) }

        viewModel.onIntent(CreateBackupIntent.SaveFinished(saved = false))
        advanceUntilIdle()

        assertEquals(backup_not_saved, viewModel.state.value.errorMessage)
        assertNull(viewModel.state.value.lastBackupAt)
        assertFalse(viewModel.state.value.saved)
    }

    @Test
    fun `a failure while writing the backup is shown`() = runTest {
        backups.createResult = Result.failure(BackupError.Unknown(IllegalStateException()))
        val viewModel = createViewModel()
        viewModel.onIntent(CreateBackupIntent.PasswordChanged(strong))
        viewModel.onIntent(CreateBackupIntent.ConfirmationChanged(strong))

        val effects = collectEffects(viewModel.effect) { viewModel.onIntent(CreateBackupIntent.CreateClicked) }

        assertTrue(effects.isEmpty())
        assertEquals(error_unexpected, viewModel.state.value.errorMessage)
    }

    @Test
    fun `the sign-in screen restores after confirming without a session password`() = runTest {
        val viewModel = restoreViewModel(fromSignIn = true)
        assertFalse(viewModel.state.value.canSubmit)
        viewModel.onIntent(RestoreBackupIntent.FilePicked("copia.physiobackup", byteArrayOf(1)))
        viewModel.onIntent(RestoreBackupIntent.PasswordChanged(strong))
        assertTrue(viewModel.state.value.canSubmit)

        viewModel.onIntent(RestoreBackupIntent.RestoreClicked)
        assertTrue(viewModel.state.value.showConfirmation)
        assertNull(backups.lastRestore)

        val effects = collectEffects(viewModel.effect) { viewModel.onIntent(RestoreBackupIntent.RestoreConfirmed) }

        assertEquals(listOf<RestoreBackupEffect>(RestoreBackupEffect.Restored), effects)
        assertEquals(true, backups.lastRestore?.third)
        assertTrue(auth.verifiedPasswords.isEmpty())
    }

    @Test
    fun `cancelling the confirmation restores nothing`() = runTest {
        val viewModel = restoreViewModel(fromSignIn = true)
        viewModel.onIntent(RestoreBackupIntent.FilePicked("copia.physiobackup", byteArrayOf(1)))
        viewModel.onIntent(RestoreBackupIntent.PasswordChanged(strong))
        viewModel.onIntent(RestoreBackupIntent.RestoreClicked)

        viewModel.onIntent(RestoreBackupIntent.ConfirmationDismissed)
        advanceUntilIdle()

        assertFalse(viewModel.state.value.showConfirmation)
        assertNull(backups.lastRestore)
    }

    private fun TestScope.restoreFromSettings(): Pair<RestoreBackupViewModel, List<RestoreBackupEffect>> {
        val viewModel = restoreViewModel(fromSignIn = false)
        viewModel.onIntent(RestoreBackupIntent.FilePicked("copia.physiobackup", byteArrayOf(1)))
        viewModel.onIntent(RestoreBackupIntent.PasswordChanged(strong))
        assertFalse(viewModel.state.value.canSubmit, "Settings also needs the session password")
        viewModel.onIntent(RestoreBackupIntent.SessionPasswordChanged("Mi-Clave-Segura-1"))
        viewModel.onIntent(RestoreBackupIntent.RestoreClicked)
        val effects = collectEffects(viewModel.effect) { viewModel.onIntent(RestoreBackupIntent.RestoreConfirmed) }
        return viewModel to effects
    }

    @Test
    fun `settings checks the session password and ends the session`() = runTest {
        val (_, effects) = restoreFromSettings()

        assertEquals(listOf<RestoreBackupEffect>(RestoreBackupEffect.Restored), effects)
        assertEquals(listOf("Mi-Clave-Segura-1"), auth.verifiedPasswords)
        assertEquals(1, auth.logoutCalls)
    }

    @Test
    fun `a wrong session password is cleared and shown`() = runTest {
        auth.verifyPasswordResult = Result.failure(AuthError.InvalidCredentials)

        val (viewModel, effects) = restoreFromSettings()

        assertTrue(effects.isEmpty())
        assertEquals(error_wrong_password, viewModel.state.value.errorMessage)
        assertEquals("", viewModel.state.value.sessionPassword)
        assertEquals(strong, viewModel.state.value.password)
    }

    @Test
    fun `the lockout ends the session and goes to login`() = runTest {
        auth.verifyPasswordResult = Result.failure(AuthError.AccountLocked(1L))

        val (_, effects) = restoreFromSettings()

        assertEquals(listOf<RestoreBackupEffect>(RestoreBackupEffect.NavigateToLogin), effects)
    }

    @Test
    fun `backup failures are explained`() = runTest {
        val cases = mapOf(
            BackupError.WrongPasswordOrFile to error_backup_wrong_password,
            BackupError.IncompatibleVersion(madeWithNewerApp = true) to error_backup_newer,
            BackupError.DeviceHasAccounts to error_backup_device_has_accounts,
        )
        cases.forEach { (error, message) ->
            backups.restoreResult = Result.failure(error)
            val (viewModel, effects) = restoreFromSettings()
            assertTrue(effects.isEmpty())
            assertEquals(message, viewModel.state.value.errorMessage)
            assertFalse(viewModel.state.value.isRestoring)
        }
    }
}
