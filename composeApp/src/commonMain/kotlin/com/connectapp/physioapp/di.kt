package com.connectapp.physioapp

import com.connectapp.data.backup.di.backupDataModule
import com.connectapp.data.di.dataModule
import com.connectapp.data.invoices.di.invoicesDataModule
import com.connectapp.data.patients.di.patientsDataModule
import com.connectapp.data.professionals.di.professionalsDataModule
import com.connectapp.data.reports.di.reportsDataModule
import com.connectapp.domain.backup.di.backupDomainModule
import com.connectapp.domain.di.authDomainModule
import com.connectapp.domain.invoices.di.invoicesDomainModule
import com.connectapp.domain.patients.di.patientsDomainModule
import com.connectapp.domain.professionals.di.professionalsDomainModule
import com.connectapp.domain.provider.AppVersionProvider
import com.connectapp.domain.reports.di.reportsDomainModule
import com.connectapp.physioapp.config.AppVersionProviderImpl
import com.connectapp.physioapp.database.databaseModule
import com.connectapp.presentation.backup.di.backupModule
import com.connectapp.presentation.di.coreModule
import com.connectapp.presentation.forgot_password.di.forgotPasswordModule
import com.connectapp.presentation.home.di.homeModule
import com.connectapp.presentation.invoices.di.invoicesModule
import com.connectapp.presentation.login.di.loginModule
import com.connectapp.presentation.onboarding.di.onboardingModule
import com.connectapp.presentation.patients.di.patientsModule
import com.connectapp.presentation.professionals.di.professionalsModule
import com.connectapp.presentation.profile.di.profileModule
import com.connectapp.presentation.register.di.registerModule
import com.connectapp.presentation.reports.di.reportsModule
import com.connectapp.presentation.settings.di.settingsModule
import com.connectapp.presentation.splash.di.splashModule
import org.koin.dsl.module

/**
 * Todos los módulos Koin de la app: uno por módulo de dominio/datos más el compartido
 * (`coreModule`, de `:presentation`) y el módulo Koin propio de cada feature — cada uno declarado
 * dentro de su propio módulo Gradle, nunca construido aquí. `appConfigModule` registra
 * [AppVersionProvider] directamente aquí porque solo `composeApp` conoce `AppConfig`.
 *
 * PhysioApp es offline-first: no hay cliente HTTP, analítica ni telemetría que registrar.
 */
private val appConfigModule = module {
    single<AppVersionProvider> { AppVersionProviderImpl() }
}

val appModules = listOf(
    authDomainModule,
    dataModule,
    professionalsDomainModule,
    professionalsDataModule,
    patientsDomainModule,
    patientsDataModule,
    reportsDomainModule,
    reportsDataModule,
    invoicesDomainModule,
    invoicesDataModule,
    backupDomainModule,
    backupDataModule,
    databaseModule,
    appConfigModule,
    coreModule,
    splashModule,
    loginModule,
    registerModule,
    onboardingModule,
    forgotPasswordModule,
    homeModule,
    settingsModule,
    profileModule,
    professionalsModule,
    patientsModule,
    reportsModule,
    invoicesModule,
    backupModule,
)
