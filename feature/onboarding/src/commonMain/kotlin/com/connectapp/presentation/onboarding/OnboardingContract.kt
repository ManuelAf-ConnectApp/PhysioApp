package com.connectapp.presentation.onboarding

import com.connectapp.domain.shared.professional.ProfessionalCollege
import com.connectapp.domain.validation.AuthField
import com.connectapp.domain.validation.AuthValidation
import org.jetbrains.compose.resources.StringResource

data class OnboardingState(
    val nif: String = "",
    val college: ProfessionalCollege? = null,
    val licenseNumber: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val isLoading: Boolean = false,
    val errorMessage: StringResource? = null,
) {
    val invalidFields: Set<AuthField>
        get() = AuthValidation.professionalProfileErrors(nif, college, licenseNumber, firstName, lastName)

    val canSubmit: Boolean get() = invalidFields.isEmpty() && !isLoading

    // Personal data never reaches logs.
    override fun toString(): String = "OnboardingState(isLoading=$isLoading, hasError=${errorMessage != null})"
}

sealed interface OnboardingIntent {
    data class NifChanged(val value: String) : OnboardingIntent
    data class CollegeSelected(val college: ProfessionalCollege) : OnboardingIntent
    data class LicenseNumberChanged(val value: String) : OnboardingIntent
    data class FirstNameChanged(val value: String) : OnboardingIntent
    data class LastNameChanged(val value: String) : OnboardingIntent
    object SaveClicked : OnboardingIntent
    object LogoutClicked : OnboardingIntent
}

sealed interface OnboardingEffect {
    object NavigateToHome : OnboardingEffect
    object NavigateToLogin : OnboardingEffect
}
