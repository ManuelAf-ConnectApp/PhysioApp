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

            implementation(projects.commonResources)
            // Session idle timeout (D14) is enforced by the shell for every screen.
            implementation(projects.domain.auth)
        }
        commonTest.dependencies {
            implementation(projects.core.uitest)
        }
    }
}
