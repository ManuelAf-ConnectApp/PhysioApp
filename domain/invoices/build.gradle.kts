plugins {
    id("physioapp.kmp.base")
}

// Payment receipts (phase 9, D6): models, validation, repository contract and
// use cases. Pure Kotlin.
kotlin {
    sourceSets {
        commonMain.dependencies {
            // api: InvoiceRepository exposes Flow<...> to its callers.
            api(libs.kotlinx.coroutines.core)
            implementation(libs.koin.core)
            api(projects.domain.shared)
            // The author of a receipt is the professional of the session (D3, D5).
            api(projects.domain.auth)
        }
    }
}
