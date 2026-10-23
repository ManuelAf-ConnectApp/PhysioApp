# CleanArch - Kotlin Multiplatform Project

CleanArch is a **Kotlin Multiplatform (KMP)** project designed with a robust **Clean Architecture** and **MVI (Model-View-Intent)** pattern. It targets both **Android** and **iOS** platforms using **Compose Multiplatform** for a unified UI experience.

## 🚀 Project Overview

The main goal of this project is to demonstrate a scalable and maintainable architecture for cross-platform mobile development. It leverages the latest technologies in the Kotlin ecosystem to provide a high-quality user experience with a single codebase for most of the logic.

## 🏗️ Architecture

The project follows Clean Architecture, split into one Gradle module per feature/layer (`specs/002-feature-modularization`, `specs/003-decentralize-domain-data-di`) rather than four monolithic ones. The allowed dependency graph between them is enforced at build time — see [Module Boundaries](#module-boundaries--dependency-graph) below.

### 1. `:domain` (Core Business Logic)
*   **Pure Kotlin**: no platform-specific code, no dependency on any other layer.
*   **Models**: data structures used throughout the app (e.g., `User`).
*   **Repositories**: interface definitions for data operations.
*   **Use Cases**: one class per business rule (e.g., `LoginUseCase`, `RegisterUseCase`).

### 2. `:core:*` (Cross-Cutting Infrastructure)
Platform-facing building blocks with no business logic of their own. May depend only on `domain`; depended on by `data:*`/`composeApp` (never directly by `feature:*`, except `:core:mvi`):
*   **`:core:network`** — shared Ktor `HttpClient` configuration.
*   **`:core:storage`** — KVault-backed secure key/value storage.
*   **`:core:database`** — SQLDelight `SqlDriver` factory backing the offline cache.
*   **`:core:analytics`** — the Sentry Android/iOS bridge (`initSentry`, `trackEvent`, `setTag`).
*   **`:core:mvi`** — the shared `State`/`Intent`/`Effect`/`ViewModel` MVI contracts every `feature:*` screen builds on.

### 3. `:data:*` (Infrastructure, one per domain)
Each owns its repository implementation(s), data sources and mappers, and may depend only on `domain` and `core:*` — never on another `data:*` module:
*   **`:data:auth`** — login/register/forgot-password over Ktor, plus KVault-backed session persistence.
*   **`:data:orders`** — orders repository, offline-first: served from a local SQLDelight cache first, refreshed from the network in the background.
*   **`:data:analytics`** — analytics-consent storage and the `AnalyticsReporter` implementation backed by `:core:analytics`.

No backend is deployed/connected by this project itself; `AuthService`'s `baseUrl` is injected via DI and expected to point at a real backend when one is available. HTTP/Ktor exceptions are mapped to the typed `domain.model.AuthError` sealed hierarchy.

### 4. `:presentation` (Shared UI Shell)
Cross-feature navigation (`NavigationRoute`/`NavigationScreen`) and the top-level `MainScreen`/`MainViewModel` that hosts it. Depends only on `domain` and `commonResources`.

### 5. `:feature:*` (One Module per Screen/Flow)
`splash`, `login`, `register`, `forgot_password`, `home`, `settings`, `profile`, `edit_profile`, `orders`, `privacy_policy` — each a self-contained MVI screen (`State`/`Intent`/`Effect`/`ViewModel`/Composable) with its own Koin module. Every `feature:*` module depends only on `domain`, `commonResources` and `core:mvi` — never on another `feature:*` module or on any `data:*` module directly.

### 6. `:commonResources`
Shared strings and design tokens (`TokenResources`), consumed by every `feature:*` module and `presentation`.

### 7. `:composeApp` (Platform Entry Points)
The only module allowed to depend on anything else in the graph: wires every `data:*`/`feature:*` Koin module together (`di.kt`), and hosts the Android `Activity`/iOS `ViewController` entry points.

### Module Boundaries & Dependency Graph

The graph above isn't just convention — `specs/013-enforce-module-boundaries` added a `verifyModuleBoundaries` build task (rules defined once in `build-logic/src/main/kotlin/cleanarch/convention/ModuleBoundaries.kt`) that runs as part of `./gradlew check`/`build`. It fails the build, naming the exact offending module and the prohibited dependency it declared, the moment any module adds a project dependency outside its permitted layer (e.g. a `feature:*` module depending on `data:*` directly).

## 🛠️ Tech Stack

*   **Language**: Kotlin
*   **UI Framework**: [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)
*   **Dependency Injection**: [Koin](https://insert-koin.io/)
*   **Concurrency**: Kotlin Coroutines & Flow
*   **Navigation**: Navigation3 / Compose Navigation
*   **State Management**: MVI (Model-View-Intent)
*   **Networking**: [Ktor Client](https://ktor.io/) (OkHttp engine on Android, Darwin on iOS)
*   **Secure Storage**: [KVault](https://github.com/Liftric/KVault) (Android Keystore / iOS Keychain-backed session persistence)
*   **Offline Cache**: [SQLDelight](https://cashapp.github.io/sqldelight/) (`:core:database` provides the platform `SqlDriver`; each `:data:*` module owns its own schema — orders and profile are readable offline and refresh silently once back online)
*   **Static Analysis**: [detekt](https://detekt.dev/), one shared ruleset for every module
*   **Code Coverage**: [Kover](https://github.com/Kotlin/kotlinx-kover), aggregated across every tested module

## 🧰 Code Quality Tooling

*   **Static analysis** — `./gradlew detekt` runs [detekt](https://detekt.dev/) against every module with one shared ruleset (`config/detekt/detekt.yml`), each module keeping only its own baseline file. CI fails the build on any new finding (`specs/005-static-analysis-ci`).
*   **Module boundaries** — enforced at build time as part of `./gradlew check`/`build`; see [Module Boundaries & Dependency Graph](#module-boundaries--dependency-graph) above (`specs/013-enforce-module-boundaries`).
*   **Code coverage** — `./gradlew koverHtmlReport` (or `koverXmlReport`) aggregates line coverage across every module with tests into one report, published as a downloadable CI artifact on every run (`specs/014-code-coverage-tooling`).

CI (`.github/workflows/ci.yml`) runs build + Android lint + unit tests, both detekt jobs, and the Kover coverage report on every push/PR; the release jobs described below only start once that same commit has passed all of them.

## ✨ Key Features

- [x] **Splash Screen**: Initial loading and navigation logic.
- [x] **Authentication**:
    - Login with validation.
    - User Registration.
    - Forgot Password flow.
- [x] **Home Dashboard**: Modern dashboard with categorized information cards.
- [x] **Settings & Profile**:
    - User profile overview.
    - App preferences (Notifications, Dark Mode).
    - Account security and legal information.
- [x] **Theming**: Integrated Material 3 design system.
- [x] **Offline Support**: Orders and profile are cached locally (SQLDelight) and stay available without connectivity, refreshing silently once back online.
- [x] **Crash & Analytics Reporting**: Opt-in (off by default, toggle in Settings) crash reporting and key-flow success/failure events via Sentry — Android only for now, see `specs/011-crash-analytics-reporting/`.
- [x] **Automated Release Pipeline**: pushing a `vMAJOR.MINOR.PATCH` tag builds a signed, versioned Android AAB and a TestFlight-ready iOS archive and publishes both as GitHub Release assets — see `specs/012-release-pipeline-automation/`.

## 🏁 Getting Started

### Prerequisites
*   Android Studio Ladybug or later.
*   Xcode (for iOS development).
*   Kotlin Multiplatform plugin.

### Configuration

Set these via `gradle.properties`, `-P<key>=...`, or the matching environment variable — never commit real values:

| Property | Env var | Required | Purpose |
|---|---|---|---|
| `connectapp.authApiBaseUrl` | `CONNECTAPP_AUTH_API_BASE_URL` | Yes (build fails without it) | Backend base URL for the auth API. |
| `connectapp.sentryDsn` | `CONNECTAPP_SENTRY_DSN` | No (Sentry stays uninitialized without it) | DSN for crash/analytics reporting — see `specs/011-crash-analytics-reporting/quickstart.md`. |

### Releasing

Pushing a tag matching `v*.*.*` (e.g. `v1.4.2`) triggers `.github/workflows/ci.yml`'s release jobs — only after the same commit has already passed the existing build/lint/test/detekt jobs. Requires these repository secrets (Settings → Secrets and variables → Actions — see `specs/012-release-pipeline-automation/data-model.md`):

| Secret | Platform | Contains |
|---|---|---|
| `ANDROID_KEYSTORE_BASE64` | Android | The signing `.jks`/`.keystore`, base64-encoded |
| `ANDROID_KEYSTORE_PASSWORD` | Android | Keystore password |
| `ANDROID_KEY_ALIAS` | Android | Key alias |
| `ANDROID_KEY_PASSWORD` | Android | Key password |
| `IOS_CERTIFICATE_BASE64` | iOS | Apple distribution certificate (`.p12`), base64-encoded |
| `IOS_CERTIFICATE_PASSWORD` | iOS | `.p12` password |
| `IOS_PROVISIONING_PROFILE_BASE64` | iOS | `.mobileprovision`, base64-encoded |
| `IOS_TEAM_ID` | iOS | Apple Developer Team ID |

Without these, the corresponding release job fails immediately with a message naming the missing secret — it never falls through to a broken/unsigned build.

### Build and Run

#### Android
- From Android Studio: Select the `composeApp` run configuration.
- From Terminal:
  ```shell
  ./gradlew :composeApp:assembleDebug
  ```

#### iOS
- Open the `iosApp` directory in Xcode and run the project.
- Or use the `iosApp` run configuration in Android Studio if configured.

---

*Developed with ❤️ using Kotlin Multiplatform.*
