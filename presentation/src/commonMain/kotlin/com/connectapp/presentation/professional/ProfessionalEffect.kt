package com.connectapp.presentation.professional

sealed interface ProfessionalEffect {
    object NavigateBack : ProfessionalEffect
    data class ShowError(val message: String) : ProfessionalEffect

    object SaveSuccess : ProfessionalEffect
    data class SaveFailure(val error: String) : ProfessionalEffect
}
