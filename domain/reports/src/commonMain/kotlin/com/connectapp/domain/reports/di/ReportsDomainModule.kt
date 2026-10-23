package com.connectapp.domain.reports.di

import com.connectapp.domain.reports.AnnulReportUseCase
import com.connectapp.domain.reports.CreateReportUseCase
import com.connectapp.domain.reports.ObserveReportUseCase
import com.connectapp.domain.reports.ObserveReportVersionsUseCase
import com.connectapp.domain.reports.SearchReportsUseCase
import com.connectapp.domain.reports.SuggestPatientsUseCase
import com.connectapp.domain.reports.UpdateReportUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/** The [com.connectapp.domain.reports.ReportRepository] comes from :data:reports. */
val reportsDomainModule = module {
    factoryOf(::CreateReportUseCase)
    factoryOf(::UpdateReportUseCase)
    factoryOf(::AnnulReportUseCase)
    factoryOf(::ObserveReportUseCase)
    factoryOf(::ObserveReportVersionsUseCase)
    factoryOf(::SearchReportsUseCase)
    factoryOf(::SuggestPatientsUseCase)
}
