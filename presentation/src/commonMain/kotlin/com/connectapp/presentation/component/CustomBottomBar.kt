package com.connectapp.presentation.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.connectapp.domain.model.User
import com.connectapp.presentation.navigation.NavigationRoute
import com.connectapp.designresources.TokensResources
import com.connectapp.designresources.DimensResources
import org.jetbrains.compose.resources.stringResource

/**
 * Representa un elemento individual en la barra de navegación inferior.
 */
private data class BottomNavItem(
    val route: NavigationRoute,
    val icon: ImageVector,
    val label: String
)

/**
 * Componente de barra de navegación inferior personalizado y robusto basado en Material 3.
 *
 * @param currentRoute La ruta actualmente activa para resaltar el icono correspondiente.
 * @param onNavigate Callback que se dispara cuando el usuario selecciona una nueva ruta.
 */
@Composable
fun CustomBottomBar(
    currentRoute: NavigationRoute,
    user: User?,
    onNavigate: (NavigationRoute) -> Unit
) {
    // Definición de los destinos de la barra inferior. 
    // Esto se podría mover a un objeto de configuración si crece mucho.
    val navItems = listOf(
        BottomNavItem(
            route = user?.let { NavigationRoute.HomeRoute(it) } ?: NavigationRoute.LoginRoute,
            icon = Icons.Default.Home,
            label = stringResource(TokensResources.home)
        ),
        BottomNavItem(
            route = NavigationRoute.ProfileRoute,
            icon = Icons.Default.Person,
            label = stringResource(TokensResources.profile)
        ),
        BottomNavItem(
            route = NavigationRoute.SettingsRoute,
            icon = Icons.Default.Settings,
            label = stringResource(TokensResources.settings)
        )
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = DimensResources.spacing4 // Elevación sutil para separar del contenido
    ) {
        navItems.forEach { item ->
            val isSelected = currentRoute == item.route
            
            NavigationBarItem(
                selected = isSelected,
                label = { 
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelMedium
                    ) 
                },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                },
                onClick = {
                    // Solo navegamos si no estamos ya en esa ruta para evitar redundancia
                    if (!isSelected) {
                        onNavigate(item.route)
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
