# CleanArch Wiki

> Contenido pensado para pegarse como página `Home` de la Wiki de GitHub del repositorio
> (`https://github.com/ManuelAf-ConnectApp/CleanArch/wiki`), o para usarse como base al
> clonar `CleanArch.wiki.git` y dividirlo en páginas independientes (una por cada sección
> `##` de este documento). Mantenlo sincronizado con `README.md`: el README es la puerta de
> entrada rápida para quien clona el repo, esta Wiki es la referencia extendida (arquitectura,
> decisiones, troubleshooting).

## Índice

- [Visión general](#visión-general)
- [Arquitectura](#arquitectura)
- [Módulos y grafo de dependencias](#módulos-y-grafo-de-dependencias)
- [Stack tecnológico](#stack-tecnológico)
- [Calidad de código](#calidad-de-código)
- [Puesta en marcha](#puesta-en-marcha)
- [Pipeline de release](#pipeline-de-release)
- [Troubleshooting](#troubleshooting)
- [Glosario de specs](#glosario-de-specs)

## Visión general

CleanArch es un proyecto **Kotlin Multiplatform (KMP)** con **Clean Architecture** + **MVI
(Model-View-Intent)**, dirigido a **Android** e **iOS** con **Compose Multiplatform** como capa
de UI compartida. El objetivo es demostrar una arquitectura escalable y mantenible para
desarrollo cross-platform, con una única base de código para la mayor parte de la lógica.

## Arquitectura

El proyecto sigue Clean Architecture dividida en un módulo Gradle por feature/capa
(`specs/002-feature-modularization`, `specs/003-decentralize-domain-data-di`), en vez de en
cuatro módulos monolíticos. El grafo de dependencias permitido entre ellos se fuerza en
build-time — ver [Módulos y grafo de dependencias](#módulos-y-grafo-de-dependencias).

### 1. `:domain` — lógica de negocio pura
- Kotlin puro: sin código específico de plataforma, sin dependencias de otras capas.
- **Modelos**: estructuras de datos usadas en toda la app (p. ej. `User`).
- **Repositorios**: definiciones de interfaz para operaciones de datos.
- **Casos de uso**: una clase por regla de negocio (`LoginUseCase`, `RegisterUseCase`, …).

### 2. `:core:*` — infraestructura transversal
Bloques de construcción orientados a plataforma sin lógica de negocio propia. Solo pueden
depender de `domain`; de ellos dependen `data:*`/`composeApp` (nunca `feature:*` directamente,
salvo `:core:mvi`):
- **`:core:network`** — configuración compartida del `HttpClient` de Ktor.
- **`:core:storage`** — almacenamiento seguro clave/valor respaldado por KVault.
- **`:core:database`** — factoría del `SqlDriver` de SQLDelight que respalda la caché offline.
- **`:core:analytics`** — puente Sentry Android/iOS (`initSentry`, `trackEvent`, `setTag`).
- **`:core:mvi`** — contratos MVI compartidos (`State`/`Intent`/`Effect`/`ViewModel`) sobre los
  que se construye cada pantalla `feature:*`.

### 3. `:data:*` — infraestructura, una por dominio
Cada uno posee su(s) implementación(es) de repositorio, data sources y mappers, y solo puede
depender de `domain` y `core:*` — nunca de otro `data:*`:
- **`:data:auth`** — login/registro/olvido de contraseña sobre Ktor, más persistencia de
  sesión respaldada por KVault.
- **`:data:orders`** — repositorio de pedidos, offline-first: se sirve primero desde caché
  local SQLDelight, se refresca en segundo plano desde red.
- **`:data:analytics`** — almacenamiento de consentimiento de analítica e implementación de
  `AnalyticsReporter` respaldada por `:core:analytics`.

No hay backend desplegado/conectado por este proyecto en sí; el `baseUrl` de `AuthService` se
inyecta vía DI y se espera que apunte a un backend real cuando exista uno. Las excepciones
HTTP/Ktor se mapean a la jerarquía sellada tipada `domain.model.AuthError`.

### 4. `:presentation` — shell de UI compartido
Navegación cross-feature (`NavigationRoute`/`NavigationScreen`) y el `MainScreen`/
`MainViewModel` de nivel superior que la aloja. Solo depende de `domain` y `commonResources`.

### 5. `:feature:*` — un módulo por pantalla/flujo
`splash`, `login`, `register`, `forgot_password`, `home`, `settings`, `profile`,
`edit_profile`, `orders`, `privacy_policy` — cada uno una pantalla MVI autocontenida
(`State`/`Intent`/`Effect`/`ViewModel`/Composable) con su propio módulo Koin. Cada `feature:*`
solo depende de `domain`, `commonResources` y `core:mvi` — nunca de otro `feature:*` ni
directamente de ningún `data:*`.

### 6. `:commonResources`
Strings y design tokens compartidos (`TokenResources`), consumidos por cada `feature:*` y por
`presentation`.

### 7. `:composeApp` — puntos de entrada de plataforma
El único módulo que puede depender de cualquier otra cosa del grafo: conecta cada módulo Koin
de `data:*`/`feature:*` (`di.kt`), y aloja los puntos de entrada `Activity` (Android) /
`ViewController` (iOS).

## Módulos y grafo de dependencias

El grafo anterior no es solo convención — `specs/013-enforce-module-boundaries` añadió una
tarea de build `verifyModuleBoundaries` (reglas definidas una vez en
`build-logic/src/main/kotlin/cleanarch/convention/ModuleBoundaries.kt`) que corre como parte de
`./gradlew check`/`build`. Falla el build, nombrando el módulo infractor exacto y la
dependencia prohibida que declaró, en el momento en que cualquier módulo añade una dependencia
de proyecto fuera de su capa permitida (p. ej. un `feature:*` dependiendo de `data:*`
directamente).

```
domain
  ├── core:network / core:storage / core:database / core:analytics / core:mvi
  │     └── data:auth / data:orders / data:analytics / data:settings
  ├── presentation (+ commonResources)
  └── feature:* (+ commonResources, core:mvi)

composeApp ── depende de todo lo anterior (único módulo "raíz")
```

## Stack tecnológico

| Área | Tecnología |
|---|---|
| Lenguaje | Kotlin |
| UI | [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/) |
| Inyección de dependencias | [Koin](https://insert-koin.io/) |
| Concurrencia | Kotlin Coroutines & Flow |
| Navegación | Navigation3 (`androidx.navigation3`, vía `org.jetbrains.androidx.navigation3`) |
| Gestión de estado | MVI (Model-View-Intent) |
| Red | [Ktor Client](https://ktor.io/) (motor OkHttp en Android, Darwin en iOS) |
| Almacenamiento seguro | [KVault](https://github.com/Liftric/KVault) (Android Keystore / iOS Keychain) |
| Caché offline | [SQLDelight](https://cashapp.github.io/sqldelight/) — `:core:database` provee el `SqlDriver` de plataforma; cada `:data:*` posee su propio schema |
| Análisis estático | [detekt](https://detekt.dev/), un único ruleset compartido |
| Cobertura de código | [Kover](https://github.com/Kotlin/kotlinx-kover), agregada entre módulos |
| Crash/analítica | Sentry (Android por ahora, `specs/011-crash-analytics-reporting`) |

## Calidad de código

- **Análisis estático** — `./gradlew detekt` corre [detekt](https://detekt.dev/) contra cada
  módulo con un ruleset compartido (`config/detekt/detekt.yml`), cada módulo mantiene solo su
  propio fichero de baseline. CI falla el build ante cualquier hallazgo nuevo
  (`specs/005-static-analysis-ci`).
- **Límites entre módulos** — forzados en build-time como parte de `./gradlew check`/`build`;
  ver [Módulos y grafo de dependencias](#módulos-y-grafo-de-dependencias)
  (`specs/013-enforce-module-boundaries`).
- **Cobertura de código** — `./gradlew koverHtmlReport` (o `koverXmlReport`) agrega la
  cobertura de línea de todos los módulos con tests en un único informe, publicado como
  artefacto descargable de CI en cada ejecución (`specs/014-code-coverage-tooling`).

CI (`.github/workflows/ci.yml`) corre build + Android lint + tests unitarios, ambos jobs de
detekt, y el informe de cobertura de Kover en cada push/PR; los jobs de release descritos abajo
solo arrancan una vez que ese mismo commit ha pasado todos ellos.

## Puesta en marcha

### Requisitos previos
- Android Studio Ladybug o posterior.
- Xcode (para desarrollo iOS).
- Plugin de Kotlin Multiplatform.

### Configuración

Se fijan vía `gradle.properties`, `-P<key>=...`, o la variable de entorno equivalente — nunca
commitear valores reales:

| Propiedad | Variable de entorno | Obligatoria | Propósito |
|---|---|---|---|
| `connectapp.authApiBaseUrl` | `CONNECTAPP_AUTH_API_BASE_URL` | Sí (el build falla sin ella) | URL base del backend para la API de auth. |
| `connectapp.sentryDsn` | `CONNECTAPP_SENTRY_DSN` | No (Sentry queda sin inicializar sin ella) | DSN para crash/analítica — ver `specs/011-crash-analytics-reporting/quickstart.md`. |

### Build y ejecución

**Android** — desde Android Studio, configuración de ejecución `composeApp`; o por terminal:
```shell
./gradlew :composeApp:assembleDebug
```

**iOS** — abre el directorio `iosApp` en Xcode y ejecuta el proyecto (usa el Run Script
`Compile Kotlin Framework`, que invoca `./gradlew :composeApp:embedAndSignAppleFrameworkForXcode`
automáticamente); o la configuración de ejecución `iosApp` en Android Studio si está
configurada.

## Pipeline de release

Empujar un tag que matchee `v*.*.*` (p. ej. `v1.4.2`) dispara los jobs de release de
`.github/workflows/ci.yml` — solo después de que ese mismo commit haya pasado ya los jobs de
build/lint/test/detekt existentes. Requiere estos secretos de repositorio (Settings → Secrets
and variables → Actions — ver `specs/012-release-pipeline-automation/data-model.md`):

| Secreto | Plataforma | Contiene |
|---|---|---|
| `ANDROID_KEYSTORE_BASE64` | Android | El `.jks`/`.keystore` de firma, en base64 |
| `ANDROID_KEYSTORE_PASSWORD` | Android | Password del keystore |
| `ANDROID_KEY_ALIAS` | Android | Alias de la clave |
| `ANDROID_KEY_PASSWORD` | Android | Password de la clave |
| `IOS_CERTIFICATE_BASE64` | iOS | Certificado de distribución de Apple (`.p12`), en base64 |
| `IOS_CERTIFICATE_PASSWORD` | iOS | Password del `.p12` |
| `IOS_PROVISIONING_PROFILE_BASE64` | iOS | `.mobileprovision`, en base64 |
| `IOS_TEAM_ID` | iOS | Team ID de Apple Developer |

Sin ellos, el job de release correspondiente falla inmediatamente nombrando el secreto que
falta — nunca cae en un build roto/sin firmar.

## Troubleshooting

Problemas reales encontrados trabajando en este repo y cómo se resolvieron.

### `Timeout waiting to lock execution history cache` al arrancar Gradle

**Síntoma**: Gradle falla al iniciar con
`Timeout waiting to lock execution history cache (.../executionHistory)`, señalando un
`Owner PID` distinto al proceso actual.

**Causa**: un demonio de Gradle huérfano (a menudo lanzado por Android Studio) sigue vivo y
mantiene el lock.

**Fix**:
```shell
./gradlew --stop      # detiene los daemons de forma ordenada
./gradlew --status     # confirma que quedan STOPPED
# si el proceso sigue en `ps` tras --stop, es seguro matarlo (ya está marcado STOPPED)
kill <PID>
```

### iOS: `Undefined symbol: _sqlite3_bind_blob` (y símbolos `_sqlite3_*` similares) al enlazar en Xcode

**Síntoma**: el framework `ComposeApp` compila bien vía `./gradlew
:composeApp:linkDebugFrameworkIosSimulatorArm64`, pero Xcode falla al enlazar el `.app` final
con símbolos `_sqlite3_*` indefinidos — solo al construir *desde Xcode*, no desde Gradle solo.

**Causa raíz**: `composeApp`/`core:database`/`data:auth`/`data:orders` construyen su framework
iOS como **framework estático** (`isStatic = true`). Con un framework estático, el flag
`-lsqlite3` en `linkerOpts` (lado Gradle, necesario porque SQLDelight's native-driver /
touchlab-sqliter enlaza contra el `libsqlite3` del sistema) solo queda registrado como una
pista de auto-enlace *dentro* del framework — Xcode necesita recogerla explícitamente, y este
proyecto no tenía ningún `OTHER_LDFLAGS` ni fase "Link Binary With Libraries" en el
`.xcodeproj` que lo hiciera.

**Fix**: añadir `-lsqlite3` a `OTHER_LDFLAGS` en las configuraciones Debug y Release del target
`iosApp`, en `iosApp/iosApp.xcodeproj/project.pbxproj`.

Si el error persiste tras aplicar el fix, sospecha de caché stale: limpia
`~/Library/Developer/Xcode/DerivedData/<proyecto>-*` y ejecuta `./gradlew clean` antes de
reconstruir en Xcode.

### Un placeholder `%s` en `strings.xml` no se sustituye (se ve el `%s` literal en pantalla)

**Síntoma**: un string con formato como `"Has pulsado en %s"` se muestra tal cual, sin
sustituir el argumento — en Android y en iOS por igual (código compartido, mismo
comportamiento en ambas plataformas).

**Causa raíz**: la librería `org.jetbrains.compose.resources` (compartida, no la
`androidx.compose.ui.res` nativa de Android) implementa su propio formateador de strings, que
**solo reconoce placeholders posicionales**:
```kotlin
// components-resources, StringResourcesUtils.kt
private val SimpleStringFormatRegex = Regex("""%(\d+)\$[ds]""")
```
Un `%s` sin índice nunca hace match con ese regex, así que nunca se sustituye.

**Fix**: usar siempre `%1$s`, `%2$d`, etc. (con índice explícito) en cualquier
`composeResources/values*/strings.xml` que se consuma vía `stringResource(resource, *args)`.

### iOS: transiciones de pantalla bruscas / con solapamiento visual

**Síntoma**: en Android, `NavDisplay` (Navigation3) anima suavemente entre pantallas; en iOS
el cambio es un corte brusco, a veces con ambas pantallas visibles un frame.

**Causa raíz (confirmada)**: el ajuste de accesibilidad **"Reducir movimiento"** activado en
el simulador/dispositivo iOS pone a `0` el factor de escala de duración de animación
(`MotionDurationScaleImpl` lee `UIAccessibilityIsReduceMotionEnabled()`), lo que anula la
duración de *todas* las animaciones de Compose Multiplatform en iOS — incluidas las
transiciones por defecto de `NavDisplay` (que ya vienen con un slide + efecto "veil" de 500ms
específico para iOS) y los `ModalBottomSheet`. Bug conocido y reportado upstream: JetBrains
YouTrack **CMP-10284**.

**Mitigación aplicada en este proyecto**: `presentation/.../MainScreen.kt` muestra un loader de
pantalla completa (`CircularProgressIndicator` sobre fondo opaco) durante ~1s tras cada cambio
de ruta, **solo en iOS** (`platform() == "iOS"`), para enmascarar el corte independientemente
del ajuste del sistema. Android no se toca — ya animaba correctamente.

**No forzar/ignorar el ajuste por código**: "Reducir movimiento" es una preferencia de
accesibilidad legítima de usuarios reales; no se debe deshabilitar mediante trucos de
animación que la contradigan.

## Glosario de specs

Este repo usa una convención `specs/NNN-nombre-descriptivo` para trazar por qué existe cada
pieza de infraestructura. Referencias mencionadas en esta página:

| Spec | Qué introdujo |
|---|---|
| `002-feature-modularization` | Separación inicial en módulos por feature |
| `003-decentralize-domain-data-di` | DI declarada por cada `data:*`/`feature:*`, no centralizada |
| `005-static-analysis-ci` | detekt + baseline por módulo en CI |
| `007-persist-apply-appearance-notifications` | Persistencia de dark mode / notificaciones |
| `008`–`014` | 8 módulos iOS-targeting adicionales (`core:*`, `data:analytics`, `feature:edit_profile`, `feature:privacy_policy`) |
| `011-crash-analytics-reporting` | Integración de Sentry, opt-in |
| `012-release-pipeline-automation` | Pipeline de release firmado Android/iOS por tag |
| `013-enforce-module-boundaries` | Tarea `verifyModuleBoundaries` |
| `014-code-coverage-tooling` | Agregación de cobertura con Kover |
