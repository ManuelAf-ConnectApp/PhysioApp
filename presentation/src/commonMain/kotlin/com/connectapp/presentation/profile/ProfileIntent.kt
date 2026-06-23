package com.connectapp.presentation.profile

sealed interface ProfileIntent {
    data object LoadUser : ProfileIntent
    data object Logout : ProfileIntent
}