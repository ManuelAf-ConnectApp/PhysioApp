package com.connectapp.presentation.register.di

import com.connectapp.presentation.register.RegisterViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val registerModule = module {
    viewModelOf(::RegisterViewModel)
}
