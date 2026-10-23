package com.connectapp.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.connectapp.domain.model.SessionStatus
import com.connectapp.domain.usecase.RestoreSessionUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * Routes the app start: a remembered, non-expired session goes to Home (or to the mandatory
 * onboarding, D3); anything else goes to Login.
 */
class SplashViewModel(
    private val restoreSession: RestoreSessionUseCase,
) : ViewModel() {

    private val _effect = MutableSharedFlow<SplashEffect>()
    val effect: SharedFlow<SplashEffect> = _effect.asSharedFlow()

    init {
        viewModelScope.launch {
            delay(SPLASH_DURATION_MILLIS)
            _effect.emit(
                when (restoreSession()) {
                    SessionStatus.SIGNED_OUT -> SplashEffect.NavigateToLogin
                    SessionStatus.NEEDS_PROFESSIONAL_PROFILE -> SplashEffect.NavigateToOnboarding
                    SessionStatus.ACTIVE -> SplashEffect.NavigateToHome
                }
            )
        }
    }

    private companion object {
        const val SPLASH_DURATION_MILLIS = 1_500L
    }
}
