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

            implementation(projects.domain.professionals)
            implementation(projects.commonResources)
            implementation(projects.core.mvi)
        }
        commonTest.dependencies {
            implementation(projects.core.uitest)
        }
    }
}
