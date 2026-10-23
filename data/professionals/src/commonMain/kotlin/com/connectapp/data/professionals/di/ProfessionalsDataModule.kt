package com.connectapp.data.professionals.di

import com.connectapp.core.database.currentEpochMillis
import com.connectapp.data.professionals.ProfessionalRepositoryImpl
import com.connectapp.domain.professionals.ProfessionalRepository
import org.koin.dsl.module

/** The [com.connectapp.core.database.physio.PhysioDatabase] comes from composeApp's databaseModule. */
val professionalsDataModule = module {
    single<ProfessionalRepository> { ProfessionalRepositoryImpl(database = get(), now = ::currentEpochMillis) }
}
