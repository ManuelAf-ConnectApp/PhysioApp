package com.connectapp.presentation.search_professional

import com.connectapp.presentation.model.ProfessionalUi

sealed interface SearchProfessionalIntent {
    data class EmailChanged(val email: String) : SearchProfessionalIntent
    data class PhoneChanged(val phone: String) : SearchProfessionalIntent
    data class CollegiateNumberChanged(val collegiateNumber: String) : SearchProfessionalIntent
    data object SearchClicked : SearchProfessionalIntent

    data object CheckAllProfessionalsClicked: SearchProfessionalIntent
    data class ProfessionalSelected(val professional: ProfessionalUi) : SearchProfessionalIntent

    data object ClearSearch : SearchProfessionalIntent
}
