package com.connectapp.presentation.forgot_password

import com.connectapp.domain.validation.AuthField
import com.connectapp.domain.validation.AuthValidation
import org.jetbrains.compose.resources.StringResource

data class ForgotPasswordState(
    val email: String = "",
    val recoveryCode: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: StringResource? = null,
    /** The replacement recovery code, shown once after a successful reset (D4). */
    val newRecoveryCode: String? = null,
    val newRecoveryCodeConfirmed: Boolean = false,
) {
    val invalidFields: Set<AuthField>
        get() = AuthValidation.passwordResetErrors(email, recoveryCode, newPassword, confirmPassword)

    val canSubmit: Boolean get() = invalidFields.isEmpty() && !isLoading

    // Never leak passwords or recovery codes through logs.
    override fun toString(): String = "ForgotPasswordState(isLoading=$isLoading, reset=${newRecoveryCode != null})"
}
