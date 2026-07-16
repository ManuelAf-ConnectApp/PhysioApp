package com.connectapp.physioapp

import com.connectapp.data.database.AppDatabase
import com.connectapp.data.database.DatabaseDriverFactory
import com.connectapp.data.datasource.*
import com.connectapp.data.datasource.impl.AuthLocalDataSourceImpl
import com.connectapp.data.repository.AuthRepositoryImpl
import com.connectapp.data.repository.PatientRepositoryImpl
import com.connectapp.data.repository.UserRepositoryImpl
import com.connectapp.data.storage.DataStoreSecureStorage
import com.connectapp.domain.repository.AuthRepository
import com.connectapp.domain.repository.PatientRepository
import com.connectapp.domain.repository.UserRepository
import com.connectapp.domain.storage.SecureStorage
import com.connectapp.domain.usecase.*
import com.connectapp.domain.validator.FormValidator
import com.connectapp.presentation.MainViewModel
import com.connectapp.presentation.edit_invoice.EditInvoiceViewModel
import com.connectapp.presentation.edit_patient.EditPatientViewModel
import com.connectapp.presentation.forgot_password.ForgotPasswordViewModel
import com.connectapp.presentation.home.HomeViewModel
import com.connectapp.presentation.invoice.InvoiceViewModel
import com.connectapp.presentation.login.LoginViewModel
import com.connectapp.presentation.patient.PatientViewModel
import com.connectapp.presentation.profile.ProfileViewModel
import com.connectapp.presentation.professional.ProfessionalViewModel
import com.connectapp.presentation.report.ReportViewModel
import com.connectapp.presentation.register.RegisterViewModel
import com.connectapp.presentation.search_patient.SearchPatientViewModel
import com.connectapp.presentation.search_invoice.SearchInvoiceViewModel
import com.connectapp.presentation.settings.SettingsViewModel
import com.connectapp.presentation.splash.SplashViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.*
import org.koin.dsl.module
import org.koin.core.qualifier.named
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.connectapp.data.datasource.impl.SpecialtyDataSourceImpl
import com.connectapp.data.manager.PermissionManagerImpl
import com.connectapp.data.mapper.UserMapper
import com.connectapp.data.repository.InvoiceRepositoryImpl
import com.connectapp.data.repository.ProfessionalRepositoryImpl
import com.connectapp.data.repository.ReportRepositoryImpl
import com.connectapp.data.repository.SpecialtyRepositoryImpl
import com.connectapp.data.resources.GeneratePdfResourceImpl
import com.connectapp.domain.manager.PermissionManager
import com.connectapp.domain.repository.InvoiceRepository
import com.connectapp.domain.repository.NotificationPermissionRepository
import com.connectapp.domain.repository.ProfessionalRepository
import com.connectapp.domain.repository.ReportRepository
import com.connectapp.domain.repository.SpecialtyRepository
import com.connectapp.domain.resources.GeneratePdfResource
import com.connectapp.presentation.search_report.SearchReportViewModel
import com.connectapp.presentation.search_professional.SearchProfessionalViewModel
import kotlinx.coroutines.runBlocking
import okio.Path.Companion.toPath

expect val platformModule: Module

val presentationModule = module {
    viewModelOf(::SplashViewModel)
    viewModelOf(::LoginViewModel)
    viewModelOf(::RegisterViewModel)
    viewModelOf(::ForgotPasswordViewModel)
    viewModelOf(::MainViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::PatientViewModel)
    viewModelOf(::EditPatientViewModel)
    viewModelOf(::ProfessionalViewModel)
    viewModelOf(::InvoiceViewModel)
    viewModelOf(::ReportViewModel)
    viewModelOf(::SearchPatientViewModel)
    viewModelOf(::SettingsViewModel)
    viewModelOf(::ProfileViewModel)
    viewModelOf(::SearchProfessionalViewModel)
    viewModelOf(::SearchReportViewModel)
    viewModelOf(::EditInvoiceViewModel)
    viewModelOf(::SearchInvoiceViewModel)
}

val domainModule = module {
    factoryOf(::LoginUseCase)
    factoryOf(::RegisterUseCase)
    factoryOf(::ForgotPasswordUseCase)
    factoryOf(::GetCurrentUserUseCase)
    factoryOf(::GetPatientByEmailUseCase)
    factoryOf(::GetPatientByPhoneUseCase)
    factoryOf(::GetPatientByDNIUseCase)
    factoryOf(::SavePatientUseCase)
    factoryOf(::UpdatePatientUseCase)
    factoryOf(::SaveProfessionalUseCase)
    factoryOf(::GenerateReportPdfUseCase)
    factoryOf(::SaveInvoiceUseCase)
    factoryOf(::GetSpecialtyListUseCase)
    factoryOf(::GetSearchPatientsByNameUseCase)
    factoryOf(::GetAllProfessionalsUseCase)
    factoryOf(::GetProfessionalByCollegiateNumberUseCase)
    factoryOf(::GetProfessionalByEmailUseCase)
    factoryOf(::GetProfessionalByPhoneUseCase)
    factoryOf(::SaveReportUseCase)
    factoryOf(::SearchReportsUseCase)
    factoryOf(::ExportDatabaseUseCase)
    factoryOf(::ImportDatabaseUseCase)
    factoryOf(::LogoutUseCase)
    factoryOf(::GenerateInvoicePdfUseCase)
    factoryOf(::SearchInvoicesUseCase)
    factoryOf(::FormValidator)
    factoryOf(::NotifyOrExportPdfUseCase)
    factoryOf(::GetNotificationPermissionUseCase)
}

val dataModule = module {
    single<AppDatabase> {
        val factory: DatabaseDriverFactory = get()
        val secureStorage: SecureStorage = get()
        val passphrase = try {
            runBlocking {
                secureStorage.getSecret("db_passphrase") ?: "secure_physio_app_db_key_2024".also {
                    secureStorage.saveSecret("db_passphrase", it)
                }
            }
        } catch (e: Exception) {
            "fallback_secure_key_123"
        }
        AppDatabase(factory.createDriver(passphrase))
    }
    singleOf(::UserMapper)
    singleOf(::UserRepositoryImpl) { bind<UserRepository>() }
    singleOf(::PatientRepositoryImpl) { bind<PatientRepository>() }
    singleOf(::AuthLocalDataSourceImpl) { bind<AuthLocalDataSource>() }
    singleOf(::AuthRepositoryImpl) { bind<AuthRepository>() }
    singleOf(::ProfessionalRepositoryImpl) { bind<ProfessionalRepository>() }
    singleOf(::ReportRepositoryImpl) { bind<ReportRepository>() }
    singleOf(::InvoiceRepositoryImpl) { bind<InvoiceRepository>() }
    single<SpecialtyRepository> { SpecialtyRepositoryImpl(get()) }
    single<SpecialtyDataSource> { SpecialtyDataSourceImpl() }
    single<GeneratePdfResource> {
        GeneratePdfResourceImpl(
            width = 595.0,
            height = 842.0,
            get(),
            get(),
            get(),
        )
    }
    single<PermissionManager> { PermissionManagerImpl(get()) }

    single {
        PreferenceDataStoreFactory.createWithPath {
            val path: String = get(named("datastore_path"))
            path.toPath()
        }
    }
    single<SecureStorage> {
        DataStoreSecureStorage(
            dataStore = get(),
            cryptoManager = get()
        )
    }
}

fun appModules() = listOf(presentationModule, domainModule, dataModule, platformModule)
