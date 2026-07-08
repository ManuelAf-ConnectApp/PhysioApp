package com.connectapp.presentation.home

import com.connectapp.domain.model.User

sealed interface HomeIntent {
    data class ItemClicked(val item: DashboardItem) : HomeIntent

    data object EmailClicked: HomeIntent

    data class LoadUser(val user: User) : HomeIntent

}
