package com.connectapp.data.mapper

import com.connectapp.data.database.ReportEntity
import com.connectapp.domain.model.Report


fun ReportEntity.toDomain(): Report {
    return Report(
        id = id,
        title = title,
        date = date,
        clinicalContent = clinicalContent,
        diagnosis = diagnosis,
        treatment = treatment,
        codPatient = patientId,
        codProfessional = professionalId
    )
}
