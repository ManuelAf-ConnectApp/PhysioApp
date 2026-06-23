package com.connectapp.domain.model

sealed class DomainError(message: String? = null) : Throwable(message) {
    sealed class AuthError(message: String? = null) : DomainError(message) {
        class EmailNotFound : AuthError("Email not found")
        class InvalidUser : AuthError("Invalid user")
        class UserAlreadyExists : AuthError("User already exists")
        class RegistrationFailed : AuthError("Registration failed")
    }

    sealed class DatabaseError(message: String? = null) : DomainError(message) {
        class InsertFailed : DatabaseError("Insert failed")
        class UpdateFailed : DatabaseError("Update failed")
        class NotFound : DatabaseError("Not found")
        data class Unknown(override val message: String) : DatabaseError(message)
    }
}
