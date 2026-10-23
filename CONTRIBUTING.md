# Contributing to PhysioApp

Thanks for your interest in PhysioApp. This guide explains how to set up the project, the rules every change must follow and how to submit it.

By participating you agree to follow the [Code of Conduct](CODE_OF_CONDUCT.md). To report a vulnerability, **do not open a public issue**: follow [SECURITY.md](SECURITY.md).

## Before you start

- PhysioApp is **pre-alpha**. The plan, open decisions and phase results live in [`docs/00-auditoria-y-plan.md`](docs/00-auditoria-y-plan.md) (in Spanish). Read the current status section of the plan before proposing a feature.
- Product decisions are recorded in the plan (D1–D21). Clinic-specific matters (consent text, tax validity of receipts) belong to whoever deploys the app for a clinic, not to the shared code: if your change touches them, raise it in an issue first.
- For anything beyond a small fix, open an [issue](https://github.com/ManuelAf-ConnectApp/PhysioApp/issues) describing the problem and the proposed approach before writing code.

## Development setup

Prerequisites, build and run commands are in the [README](README.md#installation-and-setup). In short:

```sh
./gradlew :composeApp:assembleDebug          # Android
./gradlew linkDebugFrameworkIosSimulatorArm64  # Kotlin/Native for the iOS simulator
```

The iOS app builds from `iosApp/iosApp.xcodeproj` (scheme `iosApp`).

## Non-negotiable rules

PhysioApp handles **health data**. These rules are not style preferences:

1. **Offline only.** No HTTP clients, network calls, cloud services, analytics, crash reporting or telemetry SDKs. The Android app must not declare the `INTERNET` permission.
2. **No personal or clinical data in logs**, exceptions, `toString()` output or test snapshots. Mask identifiers when a model could be printed.
3. **Data stays in the encrypted database** (`PhysioDatabase`, SQLCipher). Secrets go to Keystore/Keychain through `:core:storage`, never to plain preferences or files.
4. **Never weaken authentication or cryptography** (KDF parameters, password policy, constant-time comparisons, uniform error messages that do not reveal whether an account exists) without a reviewed decision recorded in the plan.
5. **No hard-coded credentials, test accounts or debug bypasses.** A test guards against their return.

## Architecture and code

- Multi-module Clean Architecture with MVI: `:feature:*` → `:domain:*` ← `:data:*` → `:core:*`. The allowed dependencies are enforced by `verifyModuleBoundaries` (rules in [`ModuleBoundaries.kt`](build-logic/src/main/kotlin/physioapp/convention/ModuleBoundaries.kt)); `./gradlew build` fails if a module depends on a disallowed layer.
- New modules use the convention plugins in `build-logic/` (`physioapp.kmp.base`, `physioapp.kmp.compose`, `physioapp.module-boundaries`) and must be registered in `settings.gradle.kts`.
- Write code that reads like the surrounding code: same naming, idioms and comment density.
- detekt runs with the configuration in `config/detekt/detekt.yml` (max line length 120; functions with 7 or more parameters are flagged). Do not add new entries to the `detekt-baseline.xml` files to silence your own code; fix it or justify a local `@Suppress`.

### Strings and resources

- Shared strings live in `commonResources/src/commonMain/composeResources/values/strings.xml` (English) and `values-es/strings.xml` (Spanish). **Both files are UTF-16LE with BOM**: keep that encoding when editing them.
- Every new string must exist in both languages and needs an accessor in [`TokenResources.kt`](commonResources/src/commonMain/kotlin/com/connectapp/commonresources/TokenResources.kt).

### Database

- The schema is in `core/database/src/commonMain/sqldelight/`. While no version has been distributed, v1 can still change; once a release ships, every schema change needs a migration and must pass `./gradlew verifySqlDelightMigration`.

## Tests

Every behavior change must include tests, preferably in `commonTest` so they run on both platforms.

| Command | When |
|---|---|
| `./gradlew testAndroidHostTest testDebugUnitTest` | Always |
| `./gradlew build` | Always (compilation, Android lint, tests, module boundaries) |
| `./gradlew detekt` and `./gradlew -p build-logic detekt` | Always |
| `./gradlew verifySqlDelightMigration` | When you touch the schema |
| `./gradlew iosSimulatorArm64Test` | When you touch shared logic, crypto or the database (macOS only) |
| `./gradlew :core:database:connectedAndroidDeviceTest` | When you touch database encryption (needs a device or emulator) |

Kotlin/Native test names cannot contain commas.

**UI tests** go in `commonTest` too, in classes ending in `UiTest` that extend `com.connectapp.core.uitest.UiTest` (add `implementation(projects.core.uitest)` to the module's `commonTest` dependencies). They run under Robolectric on the Android host and natively in the iOS simulator test binary; neither launches the app. Show the screen with `setContentWithKoin(...)` using the feature's real Koin modules and a fake repository, resolve strings with `Texts` so the test passes in any language, and find fields and buttons with `textField(label)` and `clickable(text)`. Use `waitForText` for content that appears after a delay (debounced suggestions).

## Documentation

Update the documentation in the same change that alters behavior:

- [`README.md`](README.md) **and** [`README.es.md`](README.es.md) must stay in sync. Do not describe as implemented anything that does not exist yet, and only document commands you have actually run.
- Add an entry under **Unreleased** in [`CHANGELOG.md`](CHANGELOG.md).
- Record new decisions or phase results in the plan.

## Submitting changes

1. Branch from `main` with a descriptive name (for example `feature/patients-search` or `fix/login-lockout`).
2. Keep commits focused, with messages in the imperative mood ("Add patient search", not "Added…").
3. Open a pull request against `main` that explains **what** changes, **why**, its impact on data and privacy, and how you tested it (commands and results, platforms checked).
4. CI ([`.github/workflows/ci.yml`](.github/workflows/ci.yml)) must be green. Changes to authentication, cryptography, storage or the schema need an explicit security review.

## License

PhysioApp is licensed under the [Apache License 2.0](LICENSE). Unless you state otherwise, any contribution you intentionally submit is licensed under the same terms, as described in section 5 of the license. Do not add third-party code or assets without checking that their license is compatible and updating [`NOTICE`](NOTICE) when required.
