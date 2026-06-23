package com.connectapp.presentation.component

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.connectapp.domain.model.User
import com.connectapp.presentation.navigation.NavigationRoute
import com.connectapp.designresources.TokensResources
import com.connectapp.designresources.DimensResources
import org.jetbrains.compose.resources.stringResource

@Composable
fun CustomNavigationRail(
    currentRoute: NavigationRoute,
    user: User?,
    onNavigate: (NavigationRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationRail(
        modifier = modifier.fillMaxHeight(),
        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface,
        header = {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = stringResource(TokensResources.logo),
                modifier = Modifier.padding(vertical = DimensResources.spacing12)
            )
        }
    ) {
        Spacer(Modifier.weight(1f))
        
        NavigationRailItem(
            selected = currentRoute is NavigationRoute.HomeRoute,
            onClick = { 
                user?.let { onNavigate(NavigationRoute.HomeRoute(it)) }
            },
            icon = { Icon(Icons.Default.Home, contentDescription = stringResource(TokensResources.home)) },
            label = { Text(stringResource(TokensResources.home)) }
        )

        NavigationRailItem(
            selected = currentRoute is NavigationRoute.ProfileRoute,
            onClick = { onNavigate(NavigationRoute.ProfileRoute) },
            icon = { Icon(Icons.Default.Person, contentDescription = stringResource(TokensResources.profile)) },
            label = { Text(stringResource(TokensResources.profile)) }
        )

        Spacer(Modifier.weight(1f))
        
        NavigationRailItem(
            selected = currentRoute is NavigationRoute.SettingsRoute,
            onClick = { onNavigate(NavigationRoute.SettingsRoute) },
            icon = { Icon(Icons.Default.Settings, contentDescription = stringResource(TokensResources.settings)) },
            label = { Text(stringResource(TokensResources.settings)) }
        )
    }
}
