package com.connectapp.domain.di

import com.connectapp.domain.usecase.ChangePasswordUseCase
import com.connectapp.domain.usecase.CompleteProfessionalProfileUseCase
import com.connectapp.domain.usecase.CurrentAuthorUseCase
import com.connectapp.domain.usecase.ExpireIdleSessionUseCase
import com.connectapp.domain.usecase.HasAccountsUseCase
import com.connectapp.domain.usecase.LoginUseCase
import com.connectapp.domain.usecase.LogoutUseCase
import com.connectapp.domain.usecase.ObserveCurrentAccountUseCase
import com.connectapp.domain.usecase.ObserveCurrentProfessionalUseCase
import com.connectapp.domain.usecase.RecordUserActivityUseCase
import com.connectapp.domain.usecase.RegenerateRecoveryCodeUseCase
import com.connectapp.domain.usecase.RegisterUseCase
import com.connectapp.domain.usecase.ResetPasswordUseCase
import com.connectapp.domain.usecase.RestoreSessionUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/**
 * :domain:auth owns its own Koin module. The [com.connectapp.domain.policy.EpochClock] and the
 * [com.connectapp.domain.repository.AuthRepository] come from :data:auth's module.
 */
val authDomainModule = module {
    factoryOf(::LoginUseCase)
    factoryOf(::RegisterUseCase)
    factoryOf(::ResetPasswordUseCase)
    factoryOf(::ChangePasswordUseCase)
    factoryOf(::RegenerateRecoveryCodeUseCase)
    factoryOf(::CompleteProfessionalProfileUseCase)
    factoryOf(::ExpireIdleSessionUseCase)
    factoryOf(::RestoreSessionUseCase)
    factoryOf(::RecordUserActivityUseCase)
    factoryOf(::ObserveCurrentAccountUseCase)
    factoryOf(::ObserveCurrentProfessionalUseCase)
    factoryOf(::CurrentAuthorUseCase)
    factoryOf(::LogoutUseCase)
    factoryOf(::HasAccountsUseCase)
}
