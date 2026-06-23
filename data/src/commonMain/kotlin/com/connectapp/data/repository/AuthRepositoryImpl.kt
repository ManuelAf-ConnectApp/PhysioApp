package com.connectapp.data.repository

import com.connectapp.designresources.resources.Res
import com.connectapp.domain.model.User
import com.connectapp.domain.model.DomainError
import com.connectapp.domain.repository.AuthRepository
import com.connectapp.domain.storage.SecureStorage

class AuthRepositoryImpl(
    private val secureStorage: SecureStorage,
) : AuthRepository {
    override suspend fun login(
        email: String,
        password: String
    ): Result<User?> {
        val user = secureStorage.getUser(email, password)
        if (user != null) {
            secureStorage.saveUser(user)
        } else {
            return Result.failure(exception = DomainError.AuthError.InvalidUser())
        }
        return Result.success(value = user)
    }

    override suspend fun getCurrentUser(): User? {
        return secureStorage.getCurrentUser()
    }

    override suspend fun register(
        firstName: String,
        lastName: String,
        email: String,
        password: String
    ): Result<Boolean> {
        return secureStorage.register(
            firstName = firstName,
            lastName = lastName,
            email = email,
            password = password
        )
    }

    override suspend fun forgotPassword(email: String): Result<Boolean> {
        return if (secureStorage.checkEmailExists(email)) {
            Result.success(value = true)
        } else {
            Result.failure(exception = DomainError.AuthError.EmailNotFound())
        }
    }

    override suspend fun logout() {
        secureStorage.clearUser()
    }

}
