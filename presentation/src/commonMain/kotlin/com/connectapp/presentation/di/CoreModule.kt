package com.connectapp.presentation.di

import com.connectapp.presentation.MainViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val coreModule = module {
    viewModelOf(::MainViewModel)
}
