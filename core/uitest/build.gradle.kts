// Test support only: Compose UI tests shared by Android (Robolectric, host tests) and iOS
// (simulator test binary). Modules use it from commonTest; it never reaches the app.
plugins {
    id("physioapp.kmp.compose")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.compose.uiTest)
            implementation(libs.koin.compose)
        }
        androidMain.dependencies {
            api(libs.robolectric)
            api(libs.junit)
            // Declares the empty activity that the Android Compose test rule launches.
            api(libs.androidx.compose.uiTestManifest)
        }
    }
}
