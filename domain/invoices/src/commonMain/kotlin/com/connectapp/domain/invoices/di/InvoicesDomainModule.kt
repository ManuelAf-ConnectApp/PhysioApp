package com.connectapp.domain.invoices.di

import com.connectapp.domain.invoices.CreateInvoiceUseCase
import com.connectapp.domain.invoices.ObserveInvoiceUseCase
import com.connectapp.domain.invoices.SearchInvoicesUseCase
import com.connectapp.domain.invoices.SetInvoicePaidUseCase
import com.connectapp.domain.invoices.SuggestInvoicePatientsUseCase
import com.connectapp.domain.invoices.VoidInvoiceUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/** The [com.connectapp.domain.invoices.InvoiceRepository] comes from :data:invoices. */
val invoicesDomainModule = module {
    factoryOf(::CreateInvoiceUseCase)
    factoryOf(::SetInvoicePaidUseCase)
    factoryOf(::VoidInvoiceUseCase)
    factoryOf(::ObserveInvoiceUseCase)
    factoryOf(::SearchInvoicesUseCase)
    factoryOf(::SuggestInvoicePatientsUseCase)
}
