package com.connectapp.data.mapper

import com.connectapp.data.database.InvoiceEntity
import com.connectapp.domain.model.Invoice

fun InvoiceEntity.toDomain(): Invoice {
    return Invoice(
        invoiceNumber = invoiceNumber,
        patientId = patientId.toString(),
        professionalId = professionalId.toString(),
        date = date,
        concept = concept,
        amount = amount.toString(),
        isPaid = isPaid == 1L
    )
}