package com.connectapp.data.repository

import com.connectapp.data.database.AppDatabase
import com.connectapp.data.mapper.toDomain
import com.connectapp.data.storage.CryptoManager
import com.connectapp.domain.model.Patient
import com.connectapp.domain.repository.PatientRepository

class PatientRepositoryImpl(
    appDatabase: AppDatabase,
    private val cryptoManager: CryptoManager
) : PatientRepository {

    private val patientQueries = appDatabase.appDatabaseQueries

    override fun getPatients(): List<Patient> {
        return patientQueries.selectAllPatients().executeAsList().map { it.toDomain().decrypt() }
    }

    override fun searchPatients(query: String): List<Patient> {
        // En búsqueda sobre datos cifrados en el repo, filtramos en memoria para mayor seguridad
        return getPatients().filter {
            it.firstName.contains(query, ignoreCase = true) || 
            it.lastName.contains(query, ignoreCase = true) ||
            it.dni.contains(query, ignoreCase = true)
        }
    }

    override fun getPatientByEmailAddress(email: String): Patient {
        return getPatients().first { it.email == email }
    }

    override fun getPatientByPhoneNumber(phone: String): Patient {
        return getPatients().first { it.phone == phone }
    }

    override fun getPatientByDNI(dni: String): Patient {
        return getPatients().first { it.dni == dni }
    }

    override fun getPatientById(id: Long): Patient {
        return patientQueries.getPatientById(id).executeAsOne().toDomain().decrypt()
    }

    override fun savePatient(patient: Patient): Boolean {
        val encrypted = patient.encrypt()
        val inserted = patientQueries.insertPatient(
            firstName = encrypted.firstName,
            lastName = encrypted.lastName,
            birthDate = encrypted.birthDate,
            dni = encrypted.dni,
            phone = encrypted.phone,
            email = encrypted.email,
            additionalInfo = encrypted.additionalInfo
        )
        return inserted.value > 0L
    }

    override fun deletePatient(id: Long) {
        patientQueries.deletePatient(id)
    }

    override fun updatePatient(patient: Patient): Boolean {
        val encrypted = patient.encrypt()
        val updated = patientQueries.updatePatient(
            firstName = encrypted.firstName,
            lastName = encrypted.lastName,
            birthDate = encrypted.birthDate,
            dni = encrypted.dni,
            phone = encrypted.phone,
            email = encrypted.email,
            additionalInfo = encrypted.additionalInfo,
            id = patient.id
        )
        return updated.value > 0L
    }

    override fun searchPatientsByName(query: String): List<Patient> {
        return searchPatients(query)
    }

    private fun Patient.encrypt(): Patient = copy(
        firstName = cryptoManager.encrypt(firstName),
        lastName = cryptoManager.encrypt(lastName),
        birthDate = cryptoManager.encrypt(birthDate),
        dni = cryptoManager.encrypt(dni),
        phone = cryptoManager.encrypt(phone),
        email = cryptoManager.encrypt(email),
        additionalInfo = cryptoManager.encrypt(additionalInfo)
    )

    private fun Patient.decrypt(): Patient = copy(
        firstName = cryptoManager.decrypt(firstName),
        lastName = cryptoManager.decrypt(lastName),
        birthDate = cryptoManager.decrypt(birthDate),
        dni = cryptoManager.decrypt(dni),
        phone = cryptoManager.decrypt(phone),
        email = cryptoManager.decrypt(email),
        additionalInfo = cryptoManager.decrypt(additionalInfo)
    )
}
