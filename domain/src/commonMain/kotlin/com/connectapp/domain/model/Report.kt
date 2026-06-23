package com.connectapp.domain.model

data class Report(
    val id: Long = 0L,
    val title: String,
    val date: String,
    val clinicalContent: String,
    val treatment: String,
    val diagnosis: String,
    val codPatient: Long,
    val codProfessional: Long,
)
