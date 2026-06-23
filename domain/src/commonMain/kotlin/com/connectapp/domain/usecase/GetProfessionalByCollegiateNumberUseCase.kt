package com.connectapp.domain.usecase

import com.connectapp.domain.model.Professional
import com.connectapp.domain.repository.ProfessionalRepository

class GetProfessionalByCollegiateNumberUseCase(
    private val repository: ProfessionalRepository
) {
     operator fun invoke(professionalNumber: String): Professional? {
        return repository.getProfessionalByCollegiateNumber(professionalNumber)
    }
}