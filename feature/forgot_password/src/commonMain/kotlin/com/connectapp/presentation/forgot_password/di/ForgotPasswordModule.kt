package com.connectapp.presentation.forgot_password.di

import com.connectapp.presentation.forgot_password.ForgotPasswordViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val forgotPasswordModule = module {
    viewModelOf(::ForgotPasswordViewModel)
}
