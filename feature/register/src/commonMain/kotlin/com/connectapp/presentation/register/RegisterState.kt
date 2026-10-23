package com.connectapp.presentation.register

import com.connectapp.domain.validation.AuthField
import com.connectapp.domain.validation.AuthValidation
import org.jetbrains.compose.resources.StringResource

data class RegisterState(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: StringResource? = null,
    /** Shown once after a successful registration (D4); null before. */
    val recoveryCode: String? = null,
    val recoveryCodeConfirmed: Boolean = false,
) {
    val invalidFields: Set<AuthField>
        get() = AuthValidation.registrationErrors(firstName, lastName, email, password, confirmPassword)

    /** "Registrar" stays disabled until every field is valid. */
    val canSubmit: Boolean get() = invalidFields.isEmpty() && !isLoading

    // Never leak the password or the recovery code through logs.
    override fun toString(): String = "RegisterState(isLoading=$isLoading, registered=${recoveryCode != null})"
}
