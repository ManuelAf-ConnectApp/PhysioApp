package com.connectapp.presentation.login

import androidx.lifecycle.viewModelScope
import com.connectapp.commonresources.error_unexpected
import com.connectapp.commonresources.login_error_invalid_credentials
import com.connectapp.commonresources.login_error_locked
import com.connectapp.core.mvi.MviViewModel
import com.connectapp.domain.model.AuthError
import com.connectapp.domain.policy.EpochClock
import com.connectapp.domain.usecase.HasAccountsUseCase
import com.connectapp.domain.usecase.LoginUseCase
import kotlinx.coroutines.launch

/**
 * Every login goes through [LoginUseCase]: there is no hardcoded/development credential path
 * (the template's DEV_ADMIN bypass was removed in phase 2).
 */
class LoginViewModel(
    private val loginUseCase: LoginUseCase,
    private val clock: EpochClock,
    hasAccountsUseCase: HasAccountsUseCase,
) : MviViewModel<LoginState, LoginIntent, LoginEffect>(LoginState()) {

    init {
        viewModelScope.launch {
            val noAccounts = runCatching { !hasAccountsUseCase() }.getOrDefault(false)
            updateState { it.copy(canRestoreBackup = noAccounts) }
        }
    }

    override fun reduce(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.EmailChanged -> updateState { it.copy(email = intent.email, errorMessage = null) }
            is LoginIntent.PasswordChanged -> updateState { it.copy(password = intent.password, errorMessage = null) }
            LoginIntent.LoginClicked -> login()
            LoginIntent.RegisterClicked -> emitEffect(LoginEffect.NavigateToRegister)
            LoginIntent.ForgotPasswordClicked -> emitEffect(LoginEffect.NavigateToForgotPassword)
            LoginIntent.RestoreBackupClicked -> emitEffect(LoginEffect.NavigateToRestoreBackup)
            LoginIntent.TogglePasswordVisibility ->
                updateState { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            is LoginIntent.RememberSessionToggled -> updateState { it.copy(rememberSessionEnabled = intent.enabled) }
        }
    }

    private fun login() {
        val form = state.value
        if (form.isLoading) return
        updateState { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            loginUseCase(form.email, form.password, form.rememberSessionEnabled)
                .onSuccess { account ->
                    // The password is no longer needed: drop it from memory.
                    updateState { LoginState(canRestoreBackup = it.canRestoreBackup) }
                    val destination = if (account.hasProfessionalProfile) {
                        LoginEffect.NavigateToHome
                    } else {
                        LoginEffect.NavigateToOnboarding
                    }
                    emitEffect(destination)
                }
                .onFailure { error -> showError(error) }
        }
    }

    // A wrong email and a wrong password must be indistinguishable.
    private fun showError(error: Throwable) {
        updateState { current ->
            when (error) {
                AuthError.InvalidCredentials ->
                    current.copy(errorMessage = login_error_invalid_credentials, errorArgument = null)
                is AuthError.AccountLocked -> current.copy(
                    errorMessage = login_error_locked,
                    errorArgument = minutesUntil(error.retryAtEpochMillis).toString(),
                )
                else -> current.copy(errorMessage = error_unexpected, errorArgument = null)
            }.copy(isLoading = false, password = "")
        }
    }

    // Rounded up, and never "0 min" while still locked.
    private fun minutesUntil(epochMillis: Long): Long =
        ((epochMillis - clock.nowMillis() + MILLIS_PER_MINUTE - 1) / MILLIS_PER_MINUTE).coerceAtLeast(1)

    private companion object {
        const val MILLIS_PER_MINUTE = 60_000L
    }
}
