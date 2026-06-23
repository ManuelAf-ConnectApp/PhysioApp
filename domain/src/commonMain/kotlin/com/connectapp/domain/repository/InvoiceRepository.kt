package com.connectapp.domain.repository

import com.connectapp.domain.model.Invoice

interface InvoiceRepository {
    suspend fun saveInvoice(invoice: Invoice): Result<Boolean>
    suspend fun getInvoiceByNumber(invoiceNumber: String): Result<Invoice?>
    suspend fun getInvoicesByProfessional(professionalName: String): Result<List<Invoice>>
    suspend fun getInvoicesByPatient(patientName: String): Result<List<Invoice>>
}