package com.connectapp.data.repository

import com.connectapp.data.database.AppDatabase
import com.connectapp.data.database.InvoiceEntity
import com.connectapp.data.mapper.toDomain
import com.connectapp.domain.model.Invoice
import com.connectapp.domain.model.DomainError
import com.connectapp.domain.repository.InvoiceRepository

class InvoiceRepositoryImpl(
    appDatabase: AppDatabase
) : InvoiceRepository {

    private val invoiceQueries = appDatabase.appDatabaseQueries
    
    override suspend fun saveInvoice(invoice: Invoice): Result<Boolean> {
        return try {
            invoiceQueries.insertInvoice(
                invoiceNumber = invoice.invoiceNumber,
                patientId = invoice.patientId.toLong(),
                professionalId = invoice.professionalId.toLong(),
                amount = invoice.amount.toDouble(),
                date = invoice.date,
                concept = invoice.concept,
                isPaid = if (invoice.isPaid) 1L else 0L
            )
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(DomainError.DatabaseError.Unknown(e.message ?: "Unknown error saving invoice"))
        }
    }

    override suspend fun getInvoiceByNumber(invoiceNumber: String): Result<Invoice?> {
        return try {
            val invoice = invoiceQueries.getInvoiceByNumber(invoiceNumber).executeAsOneOrNull()?.toDomain()
            Result.success(invoice)
        } catch (e: Exception) {
            Result.failure(DomainError.DatabaseError.Unknown(e.message ?: "Unknown error getting invoice by number"))
        }
    }

    override suspend fun getInvoicesByProfessional(professionalName: String): Result<List<Invoice>> {
        return try {
            val invoices = invoiceQueries.getInvoicesByProfessionalName(professionalName).executeAsList().map { it.toDomain() }
            Result.success(invoices)
        } catch (e: Exception) {
            Result.failure(DomainError.DatabaseError.Unknown(e.message ?: "Unknown error getting invoices by professional"))
        }
    }

    override suspend fun getInvoicesByPatient(patientName: String): Result<List<Invoice>> {
        return try {
            val invoices = invoiceQueries.getInvoicesByPatientName(patientName).executeAsList().map { it.toDomain() }
            Result.success(invoices)
        } catch (e: Exception) {
            Result.failure(DomainError.DatabaseError.Unknown(e.message ?: "Unknown error getting invoices by patient"))
        }
    }
}
