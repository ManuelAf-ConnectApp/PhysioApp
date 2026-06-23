package com.connectapp.presentation.edit_patient

sealed interface EditPatientEffect {
    object NavigateBack : EditPatientEffect
    data class ShowError(val message: String) : EditPatientEffect
}
