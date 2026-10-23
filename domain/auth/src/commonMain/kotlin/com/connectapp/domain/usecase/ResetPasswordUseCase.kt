package com.connectapp.domain.usecase

import com.connectapp.domain.model.AuthError
import com.connectapp.domain.repository.AuthRepository
import com.connectapp.domain.shared.security.RecoveryCode
import com.connectapp.domain.shared.validation.EmailValidator
import com.connectapp.domain.shared.validation.PasswordPolicy

/**
 * Offline password reset with email + one-time recovery code (D4). Returns the new recovery code,
 * which replaces the used one. A malformed email or code fails with the same neutral
 * [AuthError.InvalidRecoveryData] as a wrong pair, so the outcome never reveals whether the email
 * is registered; a new password breaking the policy fails with [AuthError.InvalidInput].
 */
class ResetPasswordUseCase(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(
        email: String,
        recoveryCode: String,
        newPassword: String,
        confirmation: String,
    ): Result<String> = when {
        !PasswordPolicy.isValidConfirmation(newPassword, confirmation) -> Result.failure(AuthError.InvalidInput)
        !EmailValidator.isValid(email) || !RecoveryCode.isWellFormed(recoveryCode) ->
            Result.failure(AuthError.InvalidRecoveryData)
        else -> repository.resetPassword(email, recoveryCode, newPassword)
    }
}
