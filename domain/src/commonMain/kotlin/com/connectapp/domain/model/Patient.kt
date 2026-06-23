package com.connectapp.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Patient(
    val id: Long = 0,
    val firstName: String,
    val lastName: String,
    val birthDate: String,
    val dni: String,
    val email: String,
    val phone: String,
    val additionalInfo: String,
)
