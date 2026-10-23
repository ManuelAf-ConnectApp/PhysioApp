package com.connectapp.presentation.register

sealed interface RegisterEffect {
    object NavigateToLogin : RegisterEffect
}
