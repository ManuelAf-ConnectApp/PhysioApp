# PhysioApp

PhysioApp es una solución multiplataforma avanzada (Android e iOS) diseñada para la gestión integral de clínicas de fisioterapia. Desarrollada con **Kotlin Multiplatform (KMP)** y **Compose Multiplatform**, ofrece una experiencia fluida y coherente para que los profesionales de la salud gestionen sus pacientes, facturación e informes clínicos.

## Características Principales

-   **Gestión Integral de Pacientes:** Registro detallado, edición y búsqueda avanzada (por DNI o nombre). Seguimiento exhaustivo del historial clínico y notas evolutivas.
-   **Sistema de Facturación:** Creación y gestión de facturas asociadas a pacientes. Permite búsquedas dinámicas por número de factura, profesional o paciente, y exportación inmediata a **PDF**.
-   **Generación de Informes Clínicos:** Creación de informes detallados incluyendo diagnóstico, contenido clínico y tratamiento. Exportación profesional a formato **PDF** lista para entregar al paciente.
-   **Gestión de Profesionales:** Portal de administración para especialistas con soporte para múltiples especialidades fisioterapéuticas (deportiva, neurológica, respiratoria, suelo pélvico, etc.).
-   **Seguridad de Datos:** Sistema completo de autenticación (Login, Registro, Recuperación de contraseña) con almacenamiento seguro de credenciales mediante **KVault**.
-   **Mantenimiento y Respaldo:** Funcionalidades de importación y exportación de la base de datos local (**SQLite**) para facilitar copias de seguridad y migración de datos.
-   **Diseño Adaptativo:** Interfaz de usuario moderna basada en **Material 3** que se adapta a diferentes tamaños de pantalla y dispositivos.

## Arquitectura y Estructura

El proyecto sigue los principios de **Arquitectura Limpia (Clean Architecture)** y el patrón **MVI/MVVM+** en la capa de presentación, asegurando escalabilidad, mantenibilidad y testabilidad.

### Módulos del Proyecto

*   `:composeApp`: Punto de entrada y configuración específica por plataforma.
*   `:presentation`: Capa de UI compartida que contiene ViewModels, Screens, States e Intents.
*   `:domain`: Núcleo de la lógica de negocio, modelos y casos de uso.
*   `:data`: Implementación de repositorios, persistencia local y servicios.
*   `:designResources`: Módulo centralizado para tokens de diseño, dimensiones, colores y recursos multilingües.
*   `/iosApp`: Contenedor nativo Swift para la ejecución en el ecosistema Apple.

## Tecnologías de Vanguardia

-   **Kotlin Multiplatform (KMP) 2.4.0**
-   **Compose Multiplatform 1.11.1**
-   **Navigation 3:** Sistema de navegación reactivo de última generación.
-   **Koin 4.2.2:** Inyección de dependencias ligera y potente.
-   **SQLDelight 2.3.2:** Persistencia de datos local segura y tipada.
-   **DataStore 1.2.1:** Gestión de preferencias de usuario.
-   **KVault 1.12.0:** Almacenamiento seguro para datos sensibles.
-   **KitePDF 0.0.5:** Generación de documentos PDF multiplataforma.
-   **Material 3:** Sistema de diseño moderno y accesible.
-   **Kotlinx Coroutines & Flow:** Gestión asíncrona y reactiva del flujo de datos.

## Requisitos del Entorno

-   **Android Studio Ladybug** o superior.
-   **Xcode 16.0** o superior (para compilación iOS).
-   **JDK 17+**.

## Instalación y Ejecución

### Android
```shell
./gradlew :composeApp:assembleDebug
```

### iOS
Abrir el proyecto `/iosApp` en Xcode o utilizar la configuración de ejecución directa desde Android Studio mediante el plugin de KMP.

## Autor y Soporte

Desarrollado por **Manuel María Alconchel Fernández**.
Para reportar incidencias o solicitar soporte, contactar a: [incidencias@connectapp.es](mailto:incidencias@connectapp.es)

## Licencia

Este proyecto se distribuye bajo la licencia **Apache License 2.0**. Consulte el archivo [LICENSE](LICENSE) para más detalles.

---

Copyright (C) 2026 PhysioApp Team
