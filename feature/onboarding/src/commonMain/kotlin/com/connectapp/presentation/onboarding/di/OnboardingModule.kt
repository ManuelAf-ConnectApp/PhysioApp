package com.connectapp.presentation.onboarding.di

import com.connectapp.presentation.onboarding.OnboardingViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val onboardingModule = module {
    viewModelOf(::OnboardingViewModel)
}
