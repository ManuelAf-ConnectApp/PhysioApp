package com.connectapp.presentation.home

sealed interface HomeEffect {
    data class NavigateToDetail(val item: DashboardItem) : HomeEffect
}
