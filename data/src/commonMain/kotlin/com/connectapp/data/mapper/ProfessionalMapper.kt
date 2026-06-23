package com.connectapp.data.mapper

import com.connectapp.data.database.ProfessionalEntity
import com.connectapp.domain.model.Professional

fun ProfessionalEntity.toDomain(): Professional {
    return Professional(
        id = id,
        firstName = firstName,
        lastName = lastName,
        email = email,
        collegiateNumber = collegiateNumber,
        phone = phone,
        specialty = specialty,
        additionalInfo = additionalInfo
    )
}