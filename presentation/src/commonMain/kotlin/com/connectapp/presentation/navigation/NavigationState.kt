package com.connectapp.presentation.navigation

import com.connectapp.domain.model.User

data class NavigationState(
    val route: NavigationRoute,
    val user: User? = null,
    val invoiceId: String? = null,

)
