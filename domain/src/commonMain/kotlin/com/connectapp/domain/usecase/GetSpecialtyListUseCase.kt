package com.connectapp.domain.usecase

import com.connectapp.domain.model.Specialty
import com.connectapp.domain.repository.SpecialtyRepository

class GetSpecialtyListUseCase(
    private val specialtyRepository: SpecialtyRepository
) {
    suspend operator fun invoke(): List<Specialty> {
        return specialtyRepository.getSpecialties()
    }
}
