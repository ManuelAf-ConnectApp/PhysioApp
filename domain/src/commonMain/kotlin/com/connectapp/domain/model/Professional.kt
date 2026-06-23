package com.connectapp.domain.model

data class Professional(
    val id: Long = 0,
    val firstName: String,
    val lastName: String,
    val collegiateNumber: String,
    val email: String,
    val phone: String,
    val specialty: String,
    val additionalInfo: String,
)
