plugins {
    id("physioapp.kmp.compose")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.compose.icons.extended)
            implementation(libs.navigation3.ui)
            implementation(libs.navigation3.runtime)
            implementation(projects.domain.auth)
            // Reminder to make a backup (plan §21.7).
            implementation(projects.domain.backup)
            implementation(projects.commonResources)
            implementation(projects.core.mvi)
        }
        commonTest.dependencies {
            implementation(projects.core.uitest)
        }
    }
}
