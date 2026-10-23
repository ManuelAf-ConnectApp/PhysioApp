package com.connectapp.presentation.onboarding

import androidx.lifecycle.viewModelScope
import com.connectapp.commonresources.error_license_already_registered
import com.connectapp.commonresources.error_nif_already_registered
import com.connectapp.commonresources.error_unexpected
import com.connectapp.core.mvi.MviViewModel
import com.connectapp.domain.model.AuthError
import com.connectapp.domain.usecase.CompleteProfessionalProfileUseCase
import com.connectapp.domain.usecase.LogoutUseCase
import com.connectapp.domain.usecase.ObserveCurrentAccountUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Mandatory professional profile after the first login (D3, confirmed 2026-10-16): until it is
 * saved the account cannot reach Home. Names are prefilled from the account and can be edited.
 */
class OnboardingViewModel(
    private val completeProfessionalProfile: CompleteProfessionalProfileUseCase,
    private val logoutUseCase: LogoutUseCase,
    observeCurrentAccount: ObserveCurrentAccountUseCase,
) : MviViewModel<OnboardingState, OnboardingIntent, OnboardingEffect>(OnboardingState()) {

    init {
        viewModelScope.launch {
            val account = observeCurrentAccount().first()
            if (account == null) {
                emitEffect(OnboardingEffect.NavigateToLogin)
            } else {
                updateState { state ->
                    state.copy(
                        firstName = state.firstName.ifEmpty { account.firstName },
                        lastName = state.lastName.ifEmpty { account.lastName },
                    )
                }
            }
        }
    }

    override fun reduce(intent: OnboardingIntent) {
        when (intent) {
            is OnboardingIntent.NifChanged -> updateState { it.copy(nif = intent.value, errorMessage = null) }
            is OnboardingIntent.CollegeSelected ->
                updateState { it.copy(college = intent.college, errorMessage = null) }
            is OnboardingIntent.LicenseNumberChanged ->
                updateState { it.copy(licenseNumber = intent.value, errorMessage = null) }
            is OnboardingIntent.FirstNameChanged ->
                updateState { it.copy(firstName = intent.value, errorMessage = null) }
            is OnboardingIntent.LastNameChanged -> updateState { it.copy(lastName = intent.value, errorMessage = null) }
            OnboardingIntent.SaveClicked -> save()
            OnboardingIntent.LogoutClicked -> {
                logoutUseCase()
                emitEffect(OnboardingEffect.NavigateToLogin)
            }
        }
    }

    private fun save() {
        val form = state.value
        if (!form.canSubmit) return
        updateState { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            completeProfessionalProfile(form.nif, form.college, form.licenseNumber, form.firstName, form.lastName)
                .onSuccess {
                    updateState { it.copy(isLoading = false) }
                    emitEffect(OnboardingEffect.NavigateToHome)
                }
                .onFailure { error ->
                    if (error == AuthError.NotAuthenticated) {
                        emitEffect(OnboardingEffect.NavigateToLogin)
                    }
                    val message = when (error) {
                        AuthError.NifAlreadyRegistered -> error_nif_already_registered
                        AuthError.LicenseNumberAlreadyRegistered -> error_license_already_registered
                        else -> error_unexpected
                    }
                    updateState { it.copy(isLoading = false, errorMessage = message) }
                }
        }
    }
}
