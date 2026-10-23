package com.connectapp.data.patients.di

import com.connectapp.core.database.currentEpochMillis
import com.connectapp.data.patients.PatientRepositoryImpl
import com.connectapp.domain.patients.PatientRepository
import org.koin.dsl.module

/** The [com.connectapp.core.database.physio.PhysioDatabase] comes from composeApp's databaseModule. */
val patientsDataModule = module {
    single<PatientRepository> { PatientRepositoryImpl(database = get(), now = ::currentEpochMillis) }
}
