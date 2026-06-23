package com.connectapp.domain.model

data class Invoice(
    val invoiceNumber: String,
    val patientId: String,
    val professionalId: String,
    val date: String,
    val concept: String,
    val amount: String,
    val isPaid: Boolean
)
