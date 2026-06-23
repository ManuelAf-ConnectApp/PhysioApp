package com.connectapp.domain.usecase

import com.connectapp.domain.model.Professional
import com.connectapp.domain.repository.ProfessionalRepository

class GetProfessionalByEmailUseCase(
    private val repository: ProfessionalRepository
) {
     operator fun invoke(email: String): Professional? {
        return repository.getProfessionalByEmail(email)
    }
}