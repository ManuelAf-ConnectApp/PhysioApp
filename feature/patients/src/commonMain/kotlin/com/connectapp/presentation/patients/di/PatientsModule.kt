package com.connectapp.presentation.patients.di

import com.connectapp.presentation.patients.detail.PatientDetailViewModel
import com.connectapp.presentation.patients.form.PatientFormViewModel
import com.connectapp.presentation.patients.search.PatientSearchViewModel
import com.connectapp.presentation.patients.sign.PatientSignViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val patientsModule = module {
    viewModelOf(::PatientSearchViewModel)
    viewModel { (patientId: String?) -> PatientFormViewModel(patientId, get(), get(), get()) }
    viewModel { (patientId: String) -> PatientDetailViewModel(patientId, get(), get(), get(), get()) }
    viewModel { (patientId: String) -> PatientSignViewModel(patientId, get()) }
}
