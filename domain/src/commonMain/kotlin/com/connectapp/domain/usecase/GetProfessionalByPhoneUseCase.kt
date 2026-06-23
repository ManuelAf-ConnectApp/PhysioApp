package com.connectapp.domain.usecase

import com.connectapp.domain.model.Professional
import com.connectapp.domain.repository.ProfessionalRepository

class GetProfessionalByPhoneUseCase(
    private val repository: ProfessionalRepository
) {
    operator fun invoke(phone: String): Professional? {
        return repository.getProfessionalByCollegiateNumber(phone)
    }
}