package com.connectapp.domain.storage

import com.connectapp.domain.model.User

interface SecureStorage {
    suspend fun saveSecret(key: String, value: String)
    suspend fun getSecret(key: String): String?
    suspend fun clearSecret(key: String)

    suspend fun saveUser(user: User)
    suspend fun getUser(email: String, password: String): User?
    suspend fun getCurrentUser(): User?

    suspend fun checkEmailExists(email: String): Boolean

    suspend fun register(
        firstName: String,
        lastName: String,
        email: String,
        password: String
    ): Result<Boolean>

    suspend fun clearUser()
}
