package com.connectapp.presentation.edit_patient

import com.connectapp.domain.model.Patient

sealed interface EditPatientIntent {
    data class LoadPatient(val dni: String) : EditPatientIntent
    data class NameChanged(val name: String) : EditPatientIntent
    data class SurnameChanged(val surname: String) : EditPatientIntent
    data class EmailChanged(val email: String) : EditPatientIntent
    data class PhoneChanged(val phone: String) : EditPatientIntent
    data class DniChanged(val dni: String) : EditPatientIntent
    data class BirthDateChanged(val birthDate: String) : EditPatientIntent
    data class NotesChanged(val notes: String) : EditPatientIntent
    object SaveClicked : EditPatientIntent
    object BackClicked : EditPatientIntent
}
