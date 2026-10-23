package com.connectapp.presentation.reports.di

import com.connectapp.presentation.reports.detail.ReportDetailViewModel
import com.connectapp.presentation.reports.form.ReportFormViewModel
import com.connectapp.presentation.reports.search.ReportSearchViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val reportsModule = module {
    viewModelOf(::ReportSearchViewModel)
    viewModel { (reportId: String?) -> ReportFormViewModel(reportId, get(), get(), get(), get(), get(), get()) }
    viewModel { (reportId: String) -> ReportDetailViewModel(reportId, get(), get(), get(), get()) }
}
