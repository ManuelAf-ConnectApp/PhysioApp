package com.connectapp.presentation.professional

sealed interface ProfessionalIntent {
    data class NameChanged(val name: String) : ProfessionalIntent
    data class SurnameChanged(val surname: String) : ProfessionalIntent
    data class SpecialtyChanged(val specialty: String) : ProfessionalIntent
    data class CollegiateNumberChanged(val collegiateNumber: String) : ProfessionalIntent

    data class EmailChanged(val email: String) : ProfessionalIntent

    data class PhoneChanged(val phone: String) : ProfessionalIntent

    data class NotesChanged(val notes: String) : ProfessionalIntent

    object SaveClicked : ProfessionalIntent
    object BackClicked : ProfessionalIntent
}
