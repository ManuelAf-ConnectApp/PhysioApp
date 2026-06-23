package com.connectapp.domain.usecase

import com.connectapp.domain.model.Patient
import com.connectapp.domain.repository.PatientRepository

class GetPatientByDNIUseCase(
    private val repository: PatientRepository
) {
    operator fun invoke(dni: String): Patient {
        return repository.getPatientByDNI(dni)
    }
}