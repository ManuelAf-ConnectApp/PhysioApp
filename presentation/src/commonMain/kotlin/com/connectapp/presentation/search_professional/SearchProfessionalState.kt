package com.connectapp.presentation.search_professional

import com.connectapp.domain.model.Professional
import com.connectapp.domain.validator.model.ValidationError
import com.connectapp.presentation.model.ProfessionalUi

data class SearchProfessionalState(
    val email: String = "",
    val phone: String = "",
    val collegiateNumber: String = "",
    val emailError: ValidationError? = null,
    val phoneError: ValidationError? = null,
    val collegiateNumberError: ValidationError? = null,
    val isLoading: Boolean = false,
    val results: List<ProfessionalUi> = emptyList(),
    val selectedProfessional: ProfessionalUi? = null,
    val error: String? = null,
    val isCheckAllProfessionals: Boolean = false,
) {
    companion object {
        val DATA_EMPTY = SearchProfessionalState()
    }

    val isEmailEnabled: Boolean get() = phone.isEmpty() && collegiateNumber.isEmpty() && !isCheckAllProfessionals
    val isPhoneEnabled: Boolean get() = email.isEmpty() && collegiateNumber.isEmpty() && !isCheckAllProfessionals
    val isCollegiateNumberEnabled: Boolean get() = email.isEmpty() && phone.isEmpty() && !isCheckAllProfessionals

    val canSearch: Boolean get() = isCheckAllProfessionals ||
            (email.isNotEmpty() && emailError == null) ||
            (phone.isNotEmpty() && phoneError == null) ||
            (collegiateNumber.isNotEmpty() && collegiateNumberError == null)

}
