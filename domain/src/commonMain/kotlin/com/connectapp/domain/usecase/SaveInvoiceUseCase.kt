package com.connectapp.domain.usecase

import com.connectapp.domain.model.Invoice
import com.connectapp.domain.repository.InvoiceRepository

class SaveInvoiceUseCase(
    private val invoiceRepository: InvoiceRepository
) {
    suspend operator fun invoke(invoice: Invoice): Result<Boolean> {
        return invoiceRepository.saveInvoice(invoice)
    }
}