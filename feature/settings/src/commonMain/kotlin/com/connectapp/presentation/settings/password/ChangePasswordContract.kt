package com.connectapp.presentation.settings.password

import com.connectapp.domain.validation.AuthField
import com.connectapp.domain.validation.AuthValidation
import org.jetbrains.compose.resources.StringResource

data class ChangePasswordState(
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isSaving: Boolean = false,
    val errorMessage: StringResource? = null,
    /** The password was changed; the screen shows the confirmation instead of the form. */
    val isDone: Boolean = false,
) {
    val invalidFields: Set<AuthField>
        get() = AuthValidation.passwordChangeErrors(currentPassword, newPassword, confirmPassword)

    val canSubmit: Boolean get() = invalidFields.isEmpty() && !isSaving

    val hasUnsavedChanges: Boolean
        get() = !isDone && (currentPassword.isNotEmpty() || newPassword.isNotEmpty() || confirmPassword.isNotEmpty())

    // Never leak passwords through logs.
    override fun toString(): String = "ChangePasswordState(isSaving=$isSaving, isDone=$isDone)"
}

sealed interface ChangePasswordIntent {
    data class CurrentPasswordChanged(val value: String) : ChangePasswordIntent {
        override fun toString(): String = "CurrentPasswordChanged(***)"
    }
    data class NewPasswordChanged(val value: String) : ChangePasswordIntent {
        override fun toString(): String = "NewPasswordChanged(***)"
    }
    data class ConfirmPasswordChanged(val value: String) : ChangePasswordIntent {
        override fun toString(): String = "ConfirmPasswordChanged(***)"
    }
    object SaveClicked : ChangePasswordIntent
}

sealed interface ChangePasswordEffect {
    /** Too many wrong passwords: the session has ended (D14 lockout). */
    object NavigateToLogin : ChangePasswordEffect
}
