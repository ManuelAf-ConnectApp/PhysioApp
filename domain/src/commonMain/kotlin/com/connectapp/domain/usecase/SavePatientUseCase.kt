package com.connectapp.domain.usecase

import com.connectapp.domain.model.Patient
import com.connectapp.domain.repository.PatientRepository

class SavePatientUseCase(
    private val repository: PatientRepository
) {
    operator fun invoke(patient: Patient): Boolean {
        return repository.savePatient(patient)
    }
}
