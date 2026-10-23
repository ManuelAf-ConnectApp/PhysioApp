package com.connectapp.domain.model

/**
 * Only type of failure exception that [com.connectapp.domain.repository.AuthRepository]'s
 * `Result<T>` may carry — no other exception type should ever reach `Result.failure`.
 *
 * There are no network/server variants: PhysioApp is offline-first and never talks to a backend.
 * Errors that could reveal whether an email is registered are deliberately coarse:
 * [InvalidCredentials] covers both a wrong password and an unknown email, and
 * [InvalidRecoveryData] covers both an unknown email and a wrong recovery code.
 */
sealed class AuthError : Exception() {
    /** Wrong email or password; never says which. */
    object InvalidCredentials : AuthError()

    /** Too many failed logins for this email; retry after [retryAtEpochMillis]. */
    data class AccountLocked(val retryAtEpochMillis: Long) : AuthError()

    object EmailAlreadyRegistered : AuthError()

    /** The email and recovery code do not identify an account; never says which part failed. */
    object InvalidRecoveryData : AuthError()

    object NifAlreadyRegistered : AuthError()
    object LicenseNumberAlreadyRegistered : AuthError()

    /** Input that breaks a validation rule reached the use case (the UI should prevent it). */
    object InvalidInput : AuthError()

    /** The operation needs a signed-in account and there is none (e.g. it expired). */
    object NotAuthenticated : AuthError()

    data class Unknown(override val cause: Throwable) : AuthError()
}
