package com.connectapp.domain.usecase

import com.connectapp.domain.model.Patient
import com.connectapp.domain.repository.PatientRepository

class GetPatientByEmailUseCase(
    private val repository: PatientRepository
) {
    operator fun invoke(email: String): Patient {
        return repository.getPatientByEmailAddress(email)
    }
}
