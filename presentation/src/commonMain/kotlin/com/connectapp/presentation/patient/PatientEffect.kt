package com.connectapp.presentation.patient

sealed interface PatientEffect {
    object NavigateBack : PatientEffect
    data class ShowError(val message: String) : PatientEffect
}
