package com.connectapp.domain.usecase

import com.connectapp.domain.model.Patient
import com.connectapp.domain.model.Professional
import com.connectapp.domain.repository.PatientRepository
import com.connectapp.domain.repository.ProfessionalRepository

class SaveProfessionalUseCase(
    private val repository: ProfessionalRepository
) {
    suspend operator fun invoke(professional: Professional): Result<Boolean> {
        return repository.insertProfessional(professional)
    }
}
