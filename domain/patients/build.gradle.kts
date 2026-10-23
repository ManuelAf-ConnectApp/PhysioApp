plugins {
    id("physioapp.kmp.base")
}

// Patient records and their signed consent (phase 7): models, validation,
// repository contract and use cases. Pure Kotlin.
kotlin {
    sourceSets {
        commonMain.dependencies {
            // api: PatientRepository exposes Flow<...> to its callers.
            api(libs.kotlinx.coroutines.core)
            implementation(libs.koin.core)
            // api: the models expose IdentityDocumentType and the screens reuse the validators.
            api(projects.domain.shared)
        }
    }
}
