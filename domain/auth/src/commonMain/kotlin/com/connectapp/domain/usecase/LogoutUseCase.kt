package com.connectapp.domain.usecase

import com.connectapp.domain.repository.AuthRepository

/** Ends the local session and forgets any remembered session. */
class LogoutUseCase(
    private val authRepository: AuthRepository,
) {
    operator fun invoke() {
        authRepository.logout()
    }
}
