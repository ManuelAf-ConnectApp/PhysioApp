package com.connectapp.presentation.profile.di

import com.connectapp.presentation.profile.ProfileViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val profileModule = module {
    viewModelOf(::ProfileViewModel)
}
