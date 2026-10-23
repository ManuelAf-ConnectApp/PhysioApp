plugins {
    id("physioapp.kmp.base")
}

// Encrypted backups of the whole database (plan §21.7): contract, rules and use cases. Pure Kotlin.
kotlin {
    sourceSets {
        commonMain.dependencies {
            // api: BackupRepository exposes Flow<...> to its callers.
            api(libs.kotlinx.coroutines.core)
            implementation(libs.koin.core)
            api(projects.domain.shared)
            // Restoring from Settings re-checks the session password and then ends the session.
            api(projects.domain.auth)
        }
    }
}
