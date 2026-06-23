package com.connectapp.domain.usecase

import com.connectapp.domain.model.Invoice
import com.connectapp.domain.repository.InvoiceRepository

class SearchInvoicesUseCase(
    private val invoiceRepository: InvoiceRepository
) {
    suspend fun byNumber(invoiceNumber: String): Result<Invoice?> {
        return invoiceRepository.getInvoiceByNumber(invoiceNumber)
    }

    suspend fun byProfessional(professionalName: String): Result<List<Invoice>> {
        return invoiceRepository.getInvoicesByProfessional(professionalName)
    }

    suspend fun byPatient(patientName: String): Result<List<Invoice>> {
        return invoiceRepository.getInvoicesByPatient(patientName)
    }
}
