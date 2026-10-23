package com.connectapp.data.reports.di

import com.connectapp.core.database.currentEpochMillis
import com.connectapp.data.reports.ReportRepositoryImpl
import com.connectapp.domain.reports.ReportRepository
import org.koin.dsl.module

/** The [com.connectapp.core.database.physio.PhysioDatabase] comes from composeApp's databaseModule. */
val reportsDataModule = module {
    single<ReportRepository> { ReportRepositoryImpl(database = get(), now = ::currentEpochMillis) }
}
