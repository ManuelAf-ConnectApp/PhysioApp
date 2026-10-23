package com.connectapp.presentation.invoices.di

import com.connectapp.presentation.invoices.detail.InvoiceDetailViewModel
import com.connectapp.presentation.invoices.form.InvoiceFormViewModel
import com.connectapp.presentation.invoices.search.InvoiceSearchViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val invoicesModule = module {
    viewModelOf(::InvoiceSearchViewModel)
    viewModelOf(::InvoiceFormViewModel)
    viewModel { (invoiceId: String, exportOnOpen: Boolean) ->
        InvoiceDetailViewModel(invoiceId, exportOnOpen, get(), get(), get(), get())
    }
}
