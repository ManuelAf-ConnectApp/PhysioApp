package com.connectapp.domain.patients.di

import com.connectapp.domain.patients.AddPatientSignatureUseCase
import com.connectapp.domain.patients.CreatePatientUseCase
import com.connectapp.domain.patients.DeletePatientUseCase
import com.connectapp.domain.patients.ObservePatientSignatureImageUseCase
import com.connectapp.domain.patients.ObservePatientUseCase
import com.connectapp.domain.patients.SearchPatientsUseCase
import com.connectapp.domain.patients.SetPatientArchivedUseCase
import com.connectapp.domain.patients.UpdatePatientUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/** The [com.connectapp.domain.patients.PatientRepository] comes from :data:patients. */
val patientsDomainModule = module {
    factoryOf(::CreatePatientUseCase)
    factoryOf(::UpdatePatientUseCase)
    factoryOf(::AddPatientSignatureUseCase)
    factoryOf(::ObservePatientUseCase)
    factoryOf(::ObservePatientSignatureImageUseCase)
    factoryOf(::SearchPatientsUseCase)
    factoryOf(::SetPatientArchivedUseCase)
    factoryOf(::DeletePatientUseCase)
}
