package com.connectapp.domain.resources

import com.connectapp.domain.model.Invoice
import com.connectapp.domain.model.Report

interface GeneratePdfResource {
    suspend fun generateReportPdf(report: Report): Result<Boolean>

    suspend fun generateInvoicePdf(invoice: Invoice): Result<Boolean>

    fun notifyOrShareFile(fileName: String)
}