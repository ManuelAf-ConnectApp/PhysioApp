package com.connectapp.presentation.professionals.di

import com.connectapp.presentation.professionals.detail.ProfessionalDetailViewModel
import com.connectapp.presentation.professionals.form.ProfessionalFormViewModel
import com.connectapp.presentation.professionals.search.ProfessionalSearchViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val professionalsModule = module {
    viewModelOf(::ProfessionalSearchViewModel)
    viewModel { (professionalId: String?) -> ProfessionalFormViewModel(professionalId, get(), get(), get()) }
    viewModel { (professionalId: String) -> ProfessionalDetailViewModel(professionalId, get(), get(), get()) }
}
