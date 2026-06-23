package com.connectapp.domain.usecase

import com.connectapp.domain.model.Patient
import com.connectapp.domain.repository.PatientRepository

class GetSearchPatientsByNameUseCase(
    private val patientRepository: PatientRepository
) {
    operator fun invoke(query: String): List<Patient> {
        if (query.isBlank()) return emptyList()
        return patientRepository.searchPatients(query)
    }
}
