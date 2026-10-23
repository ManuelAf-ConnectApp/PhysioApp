package com.connectapp.data.invoices.di

import com.connectapp.core.database.currentEpochMillis
import com.connectapp.data.invoices.InvoiceRepositoryImpl
import com.connectapp.domain.invoices.InvoiceRepository
import org.koin.dsl.module

/** The [com.connectapp.core.database.physio.PhysioDatabase] comes from composeApp's databaseModule. */
val invoicesDataModule = module {
    single<InvoiceRepository> { InvoiceRepositoryImpl(database = get(), now = ::currentEpochMillis) }
}
