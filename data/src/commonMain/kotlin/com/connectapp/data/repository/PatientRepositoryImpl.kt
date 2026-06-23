package com.connectapp.data.repository

import com.connectapp.data.database.AppDatabase
import com.connectapp.data.mapper.toDomain
import com.connectapp.domain.model.Patient
import com.connectapp.domain.repository.PatientRepository

class PatientRepositoryImpl(
     appDatabase: AppDatabase
): PatientRepository {

    private val patientQueries = appDatabase.appDatabaseQueries


    override fun getPatients(): List<Patient> {
        return patientQueries.selectAllPatients().executeAsList().map { it.toDomain() }
    }

    override fun searchPatients(query: String): List<Patient> {
        return patientQueries.searchPatientsByName(query).executeAsList().map { it.toDomain() }
    }

    override fun getPatientByEmailAddress(email: String): Patient {
        return patientQueries.getPatientByEmail(email).executeAsOne().toDomain()
    }

    override fun getPatientByPhoneNumber(phone: String): Patient {
        return patientQueries.getPatientByPhone(phone).executeAsOne().toDomain()
    }

    override fun getPatientByDNI(dni: String): Patient {
        return patientQueries.getPatientByDNI(dni).executeAsOne().toDomain()
    }

    override fun getPatientById(id: Long): Patient {
        return patientQueries.getPatientById(id).executeAsOne().toDomain()
    }

    override fun savePatient(patient: Patient): Boolean {
        val inserted = patientQueries.insertPatient(
            firstName = patient.firstName,
            lastName = patient.lastName,
            birthDate = patient.birthDate,
            dni = patient.dni,
            phone = patient.phone,
            email = patient.email,
            additionalInfo = patient.additionalInfo
        )
        return inserted.value > 0L
    }

    override fun deletePatient(id: Long) {
        patientQueries.deletePatient(id)
    }

    override fun updatePatient(patient: Patient): Boolean {
        val updated = patientQueries.updatePatient(
            firstName = patient.firstName,
            lastName = patient.lastName,
            birthDate = patient.birthDate,
            dni = patient.dni,
            phone = patient.phone,
            email = patient.email,
            additionalInfo = patient.additionalInfo,
            id = patient.id
        )
        return updated.value > 0L
    }

    override fun searchPatientsByName(query: String): List<Patient> {
        return patientQueries.searchPatientsByName(query).executeAsList().map { it.toDomain() }
    }
}
