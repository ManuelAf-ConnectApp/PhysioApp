package com.connectapp.domain.usecase

import com.connectapp.domain.model.Patient
import com.connectapp.domain.repository.PatientRepository

class GetPatientByPhoneUseCase(
    private val repository: PatientRepository
) {
    operator fun invoke(phone: String): Patient {
        return repository.getPatientByPhoneNumber(phone)
    }
}
