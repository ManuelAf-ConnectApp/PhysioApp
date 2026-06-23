package com.connectapp.presentation.patient

sealed interface PatientIntent {
    data class NameChanged(val name: String) : PatientIntent
    data class SurnameChanged(val surname: String) : PatientIntent
    data class EmailChanged(val email: String) : PatientIntent
    data class PhoneChanged(val phone: String) : PatientIntent
    data class DniChanged(val dni: String) : PatientIntent
    data class BirthDateChanged(val birthDate: String) : PatientIntent
    data class NotesChanged(val notes: String) : PatientIntent

    object SaveClicked : PatientIntent

    object BackClicked : PatientIntent
}
