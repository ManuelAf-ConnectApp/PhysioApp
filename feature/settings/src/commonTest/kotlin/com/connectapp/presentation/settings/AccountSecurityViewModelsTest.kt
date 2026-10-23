package com.connectapp.presentation.settings

import com.connectapp.commonresources.error_wrong_password
import com.connectapp.domain.model.AuthError
import com.connectapp.domain.usecase.ChangePasswordUseCase
import com.connectapp.domain.usecase.RegenerateRecoveryCodeUseCase
import com.connectapp.presentation.settings.fake.FakeAuthRepository
import com.connectapp.presentation.settings.fake.RECOVERY_CODE
import com.connectapp.presentation.settings.password.ChangePasswordEffect
import com.connectapp.presentation.settings.password.ChangePasswordIntent
import com.connectapp.presentation.settings.password.ChangePasswordViewModel
import com.connectapp.presentation.settings.recoverycode.RegenerateRecoveryCodeEffect
import com.connectapp.presentation.settings.recoverycode.RegenerateRecoveryCodeIntent
import com.connectapp.presentation.settings.recoverycode.RegenerateRecoveryCodeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AccountSecurityViewModelsTest {

    private val dispatcher = StandardTestDispatcher()
    private val repository = FakeAuthRepository()
    private val changePassword by lazy { ChangePasswordViewModel(ChangePasswordUseCase(repository)) }
    private val regenerateCode by lazy { RegenerateRecoveryCodeViewModel(RegenerateRecoveryCodeUseCase(repository)) }

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun fillChangePasswordForm(confirmation: String = NEW) {
        changePassword.onIntent(ChangePasswordIntent.CurrentPasswordChanged(CURRENT))
        changePassword.onIntent(ChangePasswordIntent.NewPasswordChanged(NEW))
        changePassword.onIntent(ChangePasswordIntent.ConfirmPasswordChanged(confirmation))
    }

    // ---- Change password ----

    @Test
    fun `the form can only be sent with the current password and a valid confirmed new one`() {
        assertFalse(changePassword.state.value.canSubmit)
        assertFalse(changePassword.state.value.hasUnsavedChanges)

        fillChangePasswordForm(confirmation = "Otra-Clave-2026")
        assertFalse(changePassword.state.value.canSubmit)
        assertTrue(changePassword.state.value.hasUnsavedChanges)

        changePassword.onIntent(ChangePasswordIntent.ConfirmPasswordChanged(NEW))
        assertTrue(changePassword.state.value.canSubmit)
    }

    @Test
    fun `a successful change clears the passwords and shows the confirmation`() = runTest {
        fillChangePasswordForm()

        changePassword.onIntent(ChangePasswordIntent.SaveClicked)
        advanceUntilIdle()

        assertEquals(listOf(CURRENT, NEW), repository.lastChangePasswordArgs)
        val state = changePassword.state.value
        assertTrue(state.isDone)
        assertEquals("", state.currentPassword + state.newPassword + state.confirmPassword)
        assertFalse(state.hasUnsavedChanges)
    }

    @Test
    fun `a wrong current password is reported and has to be typed again`() = runTest {
        repository.changePasswordResult = Result.failure(AuthError.InvalidCredentials)
        fillChangePasswordForm()

        changePassword.onIntent(ChangePasswordIntent.SaveClicked)
        advanceUntilIdle()

        val state = changePassword.state.value
        assertEquals(error_wrong_password, state.errorMessage)
        assertEquals("", state.currentPassword)
        assertEquals(NEW, state.newPassword)
        assertFalse(state.isDone)
    }

    @Test
    fun `reaching the lockout while changing the password goes to login`() = runTest {
        repository.changePasswordResult = Result.failure(AuthError.AccountLocked(retryAtEpochMillis = 1L))
        val effects = mutableListOf<ChangePasswordEffect>()
        val job = launch { changePassword.effect.toList(effects) }
        fillChangePasswordForm()

        changePassword.onIntent(ChangePasswordIntent.SaveClicked)
        advanceUntilIdle()

        assertEquals(listOf<ChangePasswordEffect>(ChangePasswordEffect.NavigateToLogin), effects)
        assertFalse(changePassword.state.value.hasUnsavedChanges)
        job.cancel()
    }

    // ---- Regenerate recovery code ----

    @Test
    fun `the new code is shown once and leaving needs the stored confirmation`() = runTest {
        val effects = mutableListOf<RegenerateRecoveryCodeEffect>()
        val job = launch { regenerateCode.effect.toList(effects) }
        assertFalse(regenerateCode.state.value.canSubmit)

        regenerateCode.onIntent(RegenerateRecoveryCodeIntent.PasswordChanged(CURRENT))
        regenerateCode.onIntent(RegenerateRecoveryCodeIntent.GenerateClicked)
        advanceUntilIdle()

        assertEquals(CURRENT, repository.lastRegeneratePassword)
        assertEquals(RECOVERY_CODE, regenerateCode.state.value.newRecoveryCode)
        assertEquals("", regenerateCode.state.value.password)

        regenerateCode.onIntent(RegenerateRecoveryCodeIntent.DoneClicked)
        advanceUntilIdle()
        assertTrue(effects.isEmpty(), "Leaving must wait for the confirmation")

        regenerateCode.onIntent(RegenerateRecoveryCodeIntent.NewRecoveryCodeConfirmed(true))
        regenerateCode.onIntent(RegenerateRecoveryCodeIntent.DoneClicked)
        advanceUntilIdle()

        assertEquals(listOf<RegenerateRecoveryCodeEffect>(RegenerateRecoveryCodeEffect.Done), effects)
        assertNull(regenerateCode.state.value.newRecoveryCode)
        job.cancel()
    }

    @Test
    fun `a wrong password does not generate a code`() = runTest {
        repository.regenerateCodeResult = Result.failure(AuthError.InvalidCredentials)

        regenerateCode.onIntent(RegenerateRecoveryCodeIntent.PasswordChanged("Mala-Clave-2026"))
        regenerateCode.onIntent(RegenerateRecoveryCodeIntent.GenerateClicked)
        advanceUntilIdle()

        assertNull(regenerateCode.state.value.newRecoveryCode)
        assertEquals(error_wrong_password, regenerateCode.state.value.errorMessage)
        assertEquals("", regenerateCode.state.value.password)
    }

    @Test
    fun `reaching the lockout while regenerating goes to login`() = runTest {
        repository.regenerateCodeResult = Result.failure(AuthError.AccountLocked(retryAtEpochMillis = 1L))
        val effects = mutableListOf<RegenerateRecoveryCodeEffect>()
        val job = launch { regenerateCode.effect.toList(effects) }

        regenerateCode.onIntent(RegenerateRecoveryCodeIntent.PasswordChanged("Mala-Clave-2026"))
        regenerateCode.onIntent(RegenerateRecoveryCodeIntent.GenerateClicked)
        advanceUntilIdle()

        assertEquals(listOf<RegenerateRecoveryCodeEffect>(RegenerateRecoveryCodeEffect.NavigateToLogin), effects)
        job.cancel()
    }

    // Passwords and recovery codes must never reach logs through toString().
    @Test
    fun `states never print passwords or codes`() = runTest {
        fillChangePasswordForm()
        regenerateCode.onIntent(RegenerateRecoveryCodeIntent.PasswordChanged(CURRENT))
        regenerateCode.onIntent(RegenerateRecoveryCodeIntent.GenerateClicked)
        advanceUntilIdle()

        listOf(changePassword.state.value.toString(), regenerateCode.state.value.toString()).forEach { text ->
            assertFalse(CURRENT in text || NEW in text || RECOVERY_CODE in text, text)
        }
    }

    private companion object {
        const val CURRENT = "Actual-Clave-2026"
        const val NEW = "Nueva-Clave-2026"
    }
}
