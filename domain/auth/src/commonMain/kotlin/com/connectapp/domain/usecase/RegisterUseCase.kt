package com.connectapp.domain.usecase

import com.connectapp.domain.model.AuthError
import com.connectapp.domain.repository.AuthRepository
import com.connectapp.domain.validation.AuthValidation

/**
 * Creates a local account and returns its one-time recovery code (D4), which
 * the UI must show exactly once. Input breaking any rule fails with [AuthError.InvalidInput]
 * without touching the repository.
 */
class RegisterUseCase(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        confirmation: String,
    ): Result<String> {
        if (AuthValidation.registrationErrors(firstName, lastName, email, password, confirmation).isNotEmpty()) {
            return Result.failure(AuthError.InvalidInput)
        }
        return repository.register(firstName.trim(), lastName.trim(), email, password)
    }
}
