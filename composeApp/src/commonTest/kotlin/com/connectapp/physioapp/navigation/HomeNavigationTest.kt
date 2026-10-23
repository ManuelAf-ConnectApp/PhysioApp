package com.connectapp.physioapp.navigation

import com.connectapp.presentation.home.HomeDestination
import com.connectapp.presentation.navigation.NavigationRoute
import kotlin.test.Test
import kotlin.test.assertEquals

class HomeNavigationTest {

    @Test
    fun `every Home card opens its own section`() {
        assertEquals(NavigationRoute.homeSections, HomeDestination.entries.map { it.toRoute() })
    }
}
