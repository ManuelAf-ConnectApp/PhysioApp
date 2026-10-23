package com.connectapp.domain.professionals.di

import com.connectapp.domain.professionals.CreateProfessionalUseCase
import com.connectapp.domain.professionals.DeleteProfessionalUseCase
import com.connectapp.domain.professionals.ObserveProfessionalUseCase
import com.connectapp.domain.professionals.SearchProfessionalsUseCase
import com.connectapp.domain.professionals.SetProfessionalArchivedUseCase
import com.connectapp.domain.professionals.UpdateProfessionalUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/** The [com.connectapp.domain.professionals.ProfessionalRepository] comes from :data:professionals. */
val professionalsDomainModule = module {
    factoryOf(::CreateProfessionalUseCase)
    factoryOf(::UpdateProfessionalUseCase)
    factoryOf(::ObserveProfessionalUseCase)
    factoryOf(::SearchProfessionalsUseCase)
    factoryOf(::SetProfessionalArchivedUseCase)
    factoryOf(::DeleteProfessionalUseCase)
}
