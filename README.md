# PhysioApp

PhysioApp es una aplicación multiplataforma (Android e iOS) diseñada para la gestión integral de clínicas de fisioterapia. Desarrollada con **Kotlin Multiplatform (KMP)** y **Compose Multiplatform**, permite a los profesionales gestionar pacientes, facturas e informes de manera eficiente.

## Características

-   **Gestión de Pacientes:** Registro completo, edición y consulta de historial clínico y notas.
-   **Gestión de Facturación:** Creación y seguimiento de facturas asociadas a pacientes.
-   **Generación de Informes:** Creación de informes clínicos y exportación a formato **PDF** (usando KitePDF).
-   **Autenticación:** Sistema seguro de login, registro y recuperación de contraseña (con almacenamiento seguro mediante KVault).
-   **Búsqueda Avanzada:** Filtros dinámicos para localizar rápidamente pacientes, facturas o informes.
-   **Perfil Profesional:** Gestión de la información del especialista y configuración de la clínica.
-   **Arquitectura Limpia:** Implementación rigurosa de Clean Architecture para asegurar escalabilidad y testabilidad.
-   **Navegación:** Implementación moderna utilizando **Navigation 3**.
-   **Diseño Unificado:** Sistema de diseño propio compartido entre plataformas para una experiencia de usuario coherente.

## Estructura del Proyecto

El proyecto está organizado en módulos siguiendo los principios de separación de responsabilidades:

*   `:composeApp`: Punto de entrada de la aplicación. Contiene la configuración de plataforma y la navegación principal.
*   `:presentation`: Capa de UI compartida. Incluye ViewModels (usando Koin), Screens, States e Intents (MVI/MVVM+).
*   `:domain`: Núcleo de la lógica de negocio. Contiene Modelos, Use Cases y definiciones de Repositorios.
*   `:data`: Implementación de la persistencia de datos, repositorios y comunicación con servicios externos.
*   `:designResources`: Módulo dedicado a la consistencia visual. Contiene tokens de diseño, dimensiones, colores y recursos compartidos.
*   `/iosApp`: Proyecto nativo de iOS que actúa como contenedor para la UI compartida en dispositivos Apple.

## Tecnologías Utilizadas

-   **Kotlin Multiplatform (KMP)**
-   **Compose Multiplatform** (Android / iOS)
-   **Koin:** Inyección de dependencias.
-   **Navigation 3:** Sistema de navegación reactivo.
-   **SQLDelight:** Persistencia de datos local.
-   **DataStore:** Almacenamiento de preferencias.
-   **KVault:** Almacenamiento seguro de credenciales.
-   **KitePDF:** Generación de documentos PDF.
-   **Kotlinx Coroutines & Flow:** Gestión de asincronía y reactividad.
-   **Jetpack Lifecycle:** Gestión del ciclo de vida en código compartido.

## Requisitos y Configuración

-   Android Studio Ladybug o superior.
-   Xcode 16+ (para compilación de iOS).
-   Kotlin 2.4.0+.

### Ejecución en Android

```shell
./gradlew :composeApp:assembleDebug
```

### Ejecución en iOS

Abre el directorio `/iosApp` en Xcode o utiliza las configuraciones de ejecución en Android Studio con el plugin de KMP.

## Licencia

Este proyecto está bajo la licencia **Apache License 2.0**. Consulta el archivo [LICENCE](LICENCE) para más detalles.

---

Copyright (C) 2026 PhysioApp Team
