package com.connectapp.presentation.mapper

import com.connectapp.domain.model.Professional
import com.connectapp.domain.model.Specialty
import com.connectapp.presentation.model.ProfessionalUi


fun Professional.toUi(list: List<Specialty>): ProfessionalUi {
    return ProfessionalUi(
        firstName = this.firstName,
        lastName = this.lastName,
        email = this.email,
        phone = this.phone,
        specialty = list.find { it.id.toLong() == this.id }?.name ?: "",
        additionalInfo = this.additionalInfo
    )
}