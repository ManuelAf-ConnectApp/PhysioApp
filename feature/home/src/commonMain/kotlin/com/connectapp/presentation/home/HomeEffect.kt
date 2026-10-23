package com.connectapp.presentation.home

sealed interface HomeEffect {

    data class NavigateTo(val destination: HomeDestination) : HomeEffect

    /** Settings → create backup, from the reminder. */
    object OpenBackup : HomeEffect
}
