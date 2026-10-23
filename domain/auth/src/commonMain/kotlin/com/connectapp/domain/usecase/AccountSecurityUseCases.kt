package com.connectapp.domain.usecase

import com.connectapp.domain.model.AuthError
import com.connectapp.domain.repository.AuthRepository
import com.connectapp.domain.validation.AuthValidation

/**
 * Settings → change password (D15): current password plus a new one that follows the policy
 * (D16), typed twice. Input breaking a rule fails with [AuthError.InvalidInput]; a wrong current
 * password with [AuthError.InvalidCredentials] or, once the lockout is reached,
 * [AuthError.AccountLocked] (the session has then ended).
 */
class ChangePasswordUseCase(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(currentPassword: String, newPassword: String, confirmation: String): Result<Unit> =
        if (AuthValidation.passwordChangeErrors(currentPassword, newPassword, confirmation).isNotEmpty()) {
            Result.failure(AuthError.InvalidInput)
        } else {
            repository.changePassword(currentPassword, newPassword)
        }
}

/**
 * Settings → regenerate recovery code (D15): asks for the password and returns the new code,
 * shown once; the previous code stops working. Same failures as [ChangePasswordUseCase].
 */
class RegenerateRecoveryCodeUseCase(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(password: String): Result<String> =
        if (password.isEmpty()) {
            Result.failure(AuthError.InvalidInput)
        } else {
            repository.regenerateRecoveryCode(password)
        }
}
