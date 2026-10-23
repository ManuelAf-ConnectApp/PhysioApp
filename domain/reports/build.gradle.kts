plugins {
    id("physioapp.kmp.base")
}

// Clinical reports (phase 8): models, validation, repository contract and use
// cases. Pure Kotlin.
kotlin {
    sourceSets {
        commonMain.dependencies {
            // api: ReportRepository exposes Flow<...> to its callers.
            api(libs.kotlinx.coroutines.core)
            implementation(libs.koin.core)
            api(projects.domain.shared)
            // The author of a report is the professional of the session (D3, D17).
            api(projects.domain.auth)
        }
    }
}
