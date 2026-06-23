package com.connectapp.presentation.profile

sealed interface ProfileEffect {
    data object NavigateToLogin : ProfileEffect
}