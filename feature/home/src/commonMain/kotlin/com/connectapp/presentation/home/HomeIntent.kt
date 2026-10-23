package com.connectapp.presentation.home

sealed interface HomeIntent {
    data class DestinationClicked(val destination: HomeDestination) : HomeIntent
    object BackupReminderClicked : HomeIntent
}
