package com.connectapp.presentation.util

import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass

/**
 * Define el tipo de navegación que se debe usar según el ancho de la pantalla.
 */
enum class NavigationType {
    BOTTOM_NAVIGATION, // Para móviles (Compact)
    NAVIGATION_RAIL,   // Para tablets pequeñas o en vertical (Medium)
    PERMANENT_DRAWER   // Para tablets grandes o en horizontal (Expanded)
}

/**
 * Define el tipo de contenido (si es panel simple o doble panel).
 */
enum class ContentType {
    SINGLE_PANE, // Un solo panel (Móvil)
    DUAL_PANE    // Dos paneles: Master-Detail (Tablet/iPad)
}

/**
 * Función de utilidad para mapear el tamaño de la ventana a tipos de navegación.
 */
fun getNavigationType(widthSizeClass: WindowWidthSizeClass): NavigationType {
    return when (widthSizeClass) {
        WindowWidthSizeClass.Compact -> NavigationType.BOTTOM_NAVIGATION
        WindowWidthSizeClass.Medium -> NavigationType.NAVIGATION_RAIL
        WindowWidthSizeClass.Expanded -> NavigationType.PERMANENT_DRAWER
        else -> NavigationType.BOTTOM_NAVIGATION
    }
}

fun getContentType(widthSizeClass: WindowWidthSizeClass): ContentType {
    return when (widthSizeClass) {
        WindowWidthSizeClass.Compact -> ContentType.SINGLE_PANE
        WindowWidthSizeClass.Medium -> ContentType.SINGLE_PANE // O DUAL_PANE según el diseño
        WindowWidthSizeClass.Expanded -> ContentType.DUAL_PANE
        else -> ContentType.SINGLE_PANE
    }
}
