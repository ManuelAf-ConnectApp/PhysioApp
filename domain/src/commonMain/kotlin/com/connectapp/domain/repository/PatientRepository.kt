package com.connectapp.domain.repository

import com.connectapp.domain.model.Patient

interface PatientRepository {

    fun getPatients(): List<Patient>

    fun searchPatients(query: String): List<Patient>

    fun getPatientByEmailAddress(email: String): Patient

    fun getPatientByPhoneNumber(phone: String): Patient

    fun getPatientByDNI(dni: String): Patient

    fun getPatientById(id: Long): Patient

    fun savePatient(patient: Patient): Boolean

    fun deletePatient(id: Long)

    fun updatePatient(patient: Patient): Boolean

    fun searchPatientsByName(query: String): List<Patient>
}
