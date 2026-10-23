package com.connectapp.domain.usecase

import com.connectapp.domain.model.Account
import com.connectapp.domain.model.AuthError
import com.connectapp.domain.repository.AuthRepository

/**
 * Signs in with email and password. Blank input fails with the same generic
 * [AuthError.InvalidCredentials] as wrong credentials.
 */
class LoginUseCase(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(email: String, password: String, rememberSession: Boolean = false): Result<Account> {
        if (email.isBlank() || password.isEmpty()) return Result.failure(AuthError.InvalidCredentials)
        return repository.login(email, password, rememberSession)
    }
}
