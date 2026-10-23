plugins {
    id("physioapp.kmp.base")
}

// Professional records (phase 6): models, validation, repository contract and
// use cases. Pure Kotlin.
kotlin {
    sourceSets {
        commonMain.dependencies {
            // api: ProfessionalRepository exposes Flow<...> to its callers.
            api(libs.kotlinx.coroutines.core)
            implementation(libs.koin.core)
            // api: the models expose ProfessionalCollege and the screens reuse the validators.
            api(projects.domain.shared)
        }
    }
}
