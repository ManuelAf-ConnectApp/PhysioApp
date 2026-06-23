package com.connectapp.domain.usecase

import com.connectapp.domain.repository.AuthRepository

class RegisterUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(
        firstName: String,
        lastName: String,
        email: String,
        password: String
    ): Result<Boolean> {
        return repository.register(firstName, lastName, email, password)
    }
}
