package com.connectapp.data.mapper

import com.connectapp.data.database.PatientEntity
import com.connectapp.domain.model.Patient

fun PatientEntity.toDomain(): Patient {
    return Patient(
        id = id,
        firstName = firstName,
        lastName = lastName,
        birthDate = birthDate,
        dni = dni,
        email = email,
        phone = phone,
        additionalInfo = additionalInfo
    )
}
