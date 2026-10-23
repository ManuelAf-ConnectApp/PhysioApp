# Inventario de licencias de terceros

Inventario de lo que incluyen las versiones compiladas de PhysioApp, hecho el 2026-10-21 (fase 10). Complementa a [`NOTICE`](../NOTICE), que reproduce los avisos obligatorios.

## Método

- **Bibliotecas Android:** el classpath de ejecución de la release (`./gradlew :composeApp:dependencies --configuration releaseRuntimeClasspath`), 202 artefactos en 67 grupos. La licencia de cada uno sale de su POM publicado en Google Maven o Maven Central, en la versión resuelta. Si el POM no declara licencia, se ha consultado el repositorio del proyecto.
- **iOS:** las mismas bibliotecas Kotlin Multiplatform, más lo que solo existe en iOS: Skiko/Skia (el motor gráfico de Compose, enlazado en el framework) y SQLCipher (paquete Swift `SQLCipher.swift`).
- **Ficheros `NOTICE` de terceros:** ningún artefacto del classpath de ejecución incluye uno (búsqueda en todos los `.jar`/`.aar`, también los anidados).
- **Recursos:** no se incluyen tipografías: la app usa las del sistema, y los PDF usan la Helvetica estándar de PDF sin incrustarla. Los iconos son Material Icons, que forman parte de `androidx.compose.material` y `org.jetbrains.compose.material` (Apache-2.0). El logotipo y los iconos de la app son material propio del titular.

## Resumen

| Licencia | Qué la usa | Obligación en una distribución binaria |
|---|---|---|
| Apache-2.0 | Casi todo: AndroidX, Compose Multiplatform, Kotlin y kotlinx, Koin, SQLDelight, Touchlab (SQLiter, Stately), Tink, Gson, Guava `listenablefuture`, JSpecify, Skiko | Dar una copia de la licencia (`LICENSE` ya contiene el texto íntegro). No hay ficheros `NOTICE` de terceros que reproducir |
| MIT | KVault (`com.liftric`), almacén de la clave de la base y de la sesión en Keystore/Keychain | Incluir su aviso de copyright y de permiso → reproducido en `NOTICE` |
| BSD-3-Clause | SQLCipher Community Edition (Android e iOS); Skia (solo iOS, dentro de Skiko) | Reproducir su aviso "en la documentación u otros materiales que acompañen a la distribución" → reproducido en `NOTICE` |
| Dominio público | SQLite (base de SQLCipher) | Ninguna |

No hay licencias copyleft (GPL, LGPL, MPL o EPL) en lo que se distribuye. Todas son compatibles con publicar PhysioApp bajo Apache-2.0.

> **Avisos dentro del binario:** las licencias MIT y BSD-3-Clause exigen que el aviso acompañe al binario. Desde el 2026-10-21 la app lo cumple con **Ajustes → Licencias de código abierto**, que lista estos componentes y muestra, sin conexión, los textos completos incluidos en `commonResources/src/commonMain/composeResources/files/licenses/` (Apache-2.0, MIT de KVault, BSD-3-Clause de SQLCipher y de Skia).

## Bibliotecas incluidas en la app (Android, release)

| Grupo | Artefactos | Versión | Licencia |
|---|---|---|---|
| `androidx.activity` | 3 | 1.13.0 | Apache-2.0 |
| `androidx.annotation` | 3 | 1.4.1 … 1.9.1 | Apache-2.0 |
| `androidx.appcompat` | 2 | 1.7.1 | Apache-2.0 |
| `androidx.arch.core` | 2 | 2.2.0 | Apache-2.0 |
| `androidx.autofill` | 1 | 1.0.0 | Apache-2.0 |
| `androidx.collection` | 3 | 1.5.0 | Apache-2.0 |
| `androidx.compose.animation` | 4 | 1.11.1 | Apache-2.0 |
| `androidx.compose.foundation` | 4 | 1.11.1 | Apache-2.0 |
| `androidx.compose.material` | 6 | 1.7.6 … 1.9.4 | Apache-2.0 |
| `androidx.compose.material3` | 2 | 1.5.0-alpha08 | Apache-2.0 |
| `androidx.compose.runtime` | 8 | 1.11.1 | Apache-2.0 |
| `androidx.compose.ui` | 14 | 1.11.1 | Apache-2.0 |
| `androidx.concurrent` | 1 | 1.1.0 | Apache-2.0 |
| `androidx.core` | 3 | 1.0.0 … 1.18.0 | Apache-2.0 |
| `androidx.cursoradapter` | 1 | 1.0.0 | Apache-2.0 |
| `androidx.customview` | 2 | 1.0.0 | Apache-2.0 |
| `androidx.documentfile` | 1 | 1.0.0 | Apache-2.0 |
| `androidx.drawerlayout` | 1 | 1.0.0 | Apache-2.0 |
| `androidx.dynamicanimation` | 1 | 1.0.0 | Apache-2.0 |
| `androidx.emoji2` | 2 | 1.4.0 | Apache-2.0 |
| `androidx.fragment` | 2 | 1.8.9 | Apache-2.0 |
| `androidx.graphics` | 3 | 1.0.1 … 1.1.0 | Apache-2.0 |
| `androidx.interpolator` | 1 | 1.0.0 | Apache-2.0 |
| `androidx.legacy` | 1 | 1.0.0 | Apache-2.0 |
| `androidx.lifecycle` | 22 | 2.10.0 | Apache-2.0 |
| `androidx.loader` | 1 | 1.0.0 | Apache-2.0 |
| `androidx.localbroadcastmanager` | 1 | 1.0.0 | Apache-2.0 |
| `androidx.navigation3` | 4 | 1.1.1 | Apache-2.0 |
| `androidx.navigationevent` | 4 | 1.0.2 | Apache-2.0 |
| `androidx.print` | 1 | 1.0.0 | Apache-2.0 |
| `androidx.profileinstaller` | 1 | 1.4.0 | Apache-2.0 |
| `androidx.resourceinspection` | 1 | 1.0.1 | Apache-2.0 |
| `androidx.savedstate` | 5 | 1.4.0 | Apache-2.0 |
| `androidx.security` | 1 | 1.1.0-alpha06 | Apache-2.0 |
| `androidx.sqlite` | 4 | 2.6.2 | Apache-2.0 |
| `androidx.startup` | 1 | 1.1.1 | Apache-2.0 |
| `androidx.tracing` | 1 | 1.2.0 | Apache-2.0 |
| `androidx.transition` | 1 | 1.6.0 | Apache-2.0 |
| `androidx.vectordrawable` | 2 | 1.1.0 | Apache-2.0 |
| `androidx.versionedparcelable` | 1 | 1.1.1 | Apache-2.0 |
| `androidx.viewpager` | 1 | 1.0.0 | Apache-2.0 |
| `androidx.window` | 3 | 1.5.0 | Apache-2.0 |
| `app.cash.sqldelight` | 7 | 2.3.2 | Apache-2.0 |
| `co.touchlab` | 6 | 2.1.0 | Apache-2.0 |
| `com.google.code.gson` | 1 | 2.8.9 | Apache-2.0 |
| `com.google.crypto.tink` | 1 | 1.8.0 | Apache-2.0 |
| `com.google.guava` | 1 | 1.0 | Apache-2.0 |
| `com.liftric` | 2 | 1.12.0 | MIT |
| `io.insert-koin` | 9 | 4.2.1 | Apache-2.0 |
| `net.zetetic` | 1 | 4.17.0 | BSD-3-Clause (Zetetic LLC; licencia del repositorio sqlcipher-android, el POM no la declara) |
| `org.jetbrains` | 1 | 23.0.0 | Apache-2.0 |
| `org.jetbrains.androidx.lifecycle` | 7 | 2.10.0 | Apache-2.0 |
| `org.jetbrains.androidx.navigation3` | 1 | 1.1.1 | Apache-2.0 |
| `org.jetbrains.androidx.navigationevent` | 1 | 1.0.1 | Apache-2.0 |
| `org.jetbrains.androidx.savedstate` | 2 | 1.3.6 | Apache-2.0 |
| `org.jetbrains.compose.animation` | 2 | 1.11.0 | Apache-2.0 |
| `org.jetbrains.compose.annotation-internal` | 1 | 1.9.3 | Apache-2.0 |
| `org.jetbrains.compose.collection-internal` | 1 | 1.9.3 | Apache-2.0 |
| `org.jetbrains.compose.components` | 2 | 1.11.0 | Apache-2.0 |
| `org.jetbrains.compose.foundation` | 2 | 1.11.0 | Apache-2.0 |
| `org.jetbrains.compose.material` | 3 | 1.7.3 … 1.9.3 | Apache-2.0 |
| `org.jetbrains.compose.material3` | 1 | 1.10.0-alpha05 | Apache-2.0 |
| `org.jetbrains.compose.runtime` | 2 | 1.11.0 | Apache-2.0 |
| `org.jetbrains.compose.ui` | 9 | 1.11.0 … 1.9.3 | Apache-2.0 |
| `org.jetbrains.kotlin` | 3 | 1.9.10 … 2.3.21 | Apache-2.0 |
| `org.jetbrains.kotlinx` | 9 | 0.7.1 … 1.7.3 | Apache-2.0 |
| `org.jspecify` | 1 | 1.0.0 | Apache-2.0 |
## Solo en iOS

| Componente | Versión | Licencia | Nota |
|---|---|---|---|
| SQLCipher Community Edition (`SQLCipher.swift`, xcframework) | 4.17.0 | BSD-3-Clause (Zetetic LLC) | Aviso en `NOTICE` |
| Skiko (`org.jetbrains.skiko`) | 0.144.6 | Apache-2.0 | Motor gráfico de Compose en iOS |
| Skia (dentro de Skiko) | la de Skiko 0.144.6 | BSD-3-Clause (Google Inc.) | Aviso en `NOTICE` |

## Solo para compilar o probar (no se distribuyen)

| Herramienta | Versión | Licencia |
|---|---|---|
| Android Gradle Plugin, Kotlin, Compose Multiplatform (plugins) | ver `gradle/libs.versions.toml` | Apache-2.0 |
| detekt + detekt-formatting | 1.23.8 | Apache-2.0 |
| Kover | 0.9.9 | Apache-2.0 |
| Compose UI Test (`org.jetbrains.compose.ui:ui-test`, `androidx.compose.ui:ui-test-manifest`) | 1.11.0 / 1.11.1 | Apache-2.0 |
| kotlin-test, kotlinx-coroutines-test | ver catálogo | Apache-2.0 |
| Robolectric | 4.17 | MIT |
| JUnit 4 | 4.13.2 | EPL-1.0 (solo en tests; no se distribuye) |
| SQLDelight `sqlite-driver` (tests de esquema en JVM) | 2.3.2 | Apache-2.0 |

## Cómo actualizarlo

Cada vez que entre o cambie una dependencia de ejecución:

1. `./gradlew :composeApp:dependencies --configuration releaseRuntimeClasspath` y comparar con la tabla.
2. Comprobar la licencia en el POM de la versión resuelta (o en el repositorio si no la declara).
3. Si es MIT, BSD u otra que exija reproducir el aviso, añadirlo a `NOTICE` y a la pantalla de licencias: su texto en `files/licenses/` y su entrada en `feature/settings/.../licenses/OpenSourceLicenses.kt`. Para Apache-2.0 basta con añadir la entrada. Si trae su propio `NOTICE`, conservar su contenido.
4. Rechazar licencias copyleft sin una decisión explícita del titular.
