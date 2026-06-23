package com.connectapp.domain.usecase

import com.connectapp.domain.model.Professional
import com.connectapp.domain.repository.ProfessionalRepository

class GetAllProfessionalsUseCase(
    private val professionalRepository: ProfessionalRepository
) {
    operator fun invoke(): List<Professional> {
        return professionalRepository.getAllProfessionals()
    }
}
