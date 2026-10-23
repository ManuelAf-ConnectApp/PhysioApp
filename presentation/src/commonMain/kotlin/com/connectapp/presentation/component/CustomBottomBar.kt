package com.connectapp.presentation.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.connectapp.commonresources.nav_home
import com.connectapp.commonresources.nav_profile
import com.connectapp.commonresources.nav_settings
import com.connectapp.presentation.navigation.NavigationRoute
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Representa un elemento individual en la barra de navegación inferior.
 */
private data class BottomNavItem(
    val route: NavigationRoute,
    val icon: ImageVector,
    val label: StringResource
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
    onNavigate: (NavigationRoute) -> Unit
) {
    // Definición de los destinos de la barra inferior. 
    // Esto se podría mover a un objeto de configuración si crece mucho.
    val navItems = listOf(
        BottomNavItem(
            route = NavigationRoute.HomeRoute,
            icon = Icons.Default.Home,
            label = nav_home
        ),
        BottomNavItem(
            route = NavigationRoute.ProfileRoute,
            icon = Icons.Default.Person,
            label = nav_profile
        ),
        BottomNavItem(
            route = NavigationRoute.SettingsRoute,
            icon = Icons.Default.Settings,
            label = nav_settings
        )
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp // Elevación sutil para separar del contenido
    ) {
        navItems.forEach { item ->
            // The Home sections keep "Inicio" highlighted; tapping it there returns to Home.
            val isSelected = currentRoute.bottomBarDestination == item.route

            NavigationBarItem(
                selected = isSelected,
                label = { 
                    Text(
                        text = stringResource(item.label),
                        style = MaterialTheme.typography.labelMedium
                    ) 
                },
                icon = {
                    // The visible label already names the item; a description would be read twice.
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null
                    )
                },
                onClick = {
                    // Solo navegamos si no estamos ya en esa ruta para evitar redundancia
                    if (currentRoute != item.route) {
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
