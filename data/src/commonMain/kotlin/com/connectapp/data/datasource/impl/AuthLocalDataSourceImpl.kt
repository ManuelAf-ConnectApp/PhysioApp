package com.connectapp.data.datasource.impl

import com.connectapp.data.database.AppDatabase
import com.connectapp.data.datasource.AuthLocalDataSource
import com.connectapp.domain.model.User
import com.connectapp.domain.model.DomainError

class AuthLocalDataSourceImpl(
    database: AppDatabase
) : AuthLocalDataSource {

    private val queries = database.appDatabaseQueries

    override fun clearSession() {
    }

    override fun login(
        email: String,
        password: String
    ): User? {
        return queries.getUserByEmailAndPassword(email, password).executeAsOneOrNull()
            ?.let { userEntity ->
                User(
                    id = userEntity.id,
                    firstName = userEntity.firstName,
                    lastName = userEntity.lastName,
                    email = userEntity.email,
                    password = userEntity.password
                )
            }
    }

    override fun register(
        firstName: String,
        lastName: String,
        email: String,
        password: String
    ): Result<Boolean> {
        return try {
            val insertUser = queries.insertUser(
                firstName = firstName,
                lastName = lastName,
                email = email,
                password = password
            )
            if (insertUser.value == 0L) {
                return Result.failure(DomainError.AuthError.RegistrationFailed())
            } else {
                Result.success(true)
            }
        } catch (e: Exception) {
            Result.failure(DomainError.DatabaseError.Unknown(e.message ?: "Database error during registration"))
        }
    }

    override fun forgotPassword(email: String): Result<Unit> {
        TODO("Not yet implemented")
    }
}
