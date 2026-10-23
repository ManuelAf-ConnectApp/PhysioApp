plugins {
    id("physioapp.kmp.base")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // api (not implementation): AuthRepository exposes Flow<...> directly to callers
            // (see specs/010-offline-cache-layer/contracts/), so it must be visible on their
            // compile classpath — same criterion already used by core:network/core:storage.
            api(libs.kotlinx.coroutines.core)
            // specs/003-decentralize-domain-data-di: domain/auth/di/AuthDomainModule.kt declares
            // its own Koin module instead of composeApp constructing its use cases directly.
            implementation(libs.koin.core)
            // api: AuthValidation's callers (feature modules) use the same validators for hints.
            api(projects.domain.shared)
        }
    }
}
