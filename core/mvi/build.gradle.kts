plugins {
    id("physioapp.kmp.base")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // api (not implementation): StateViewModel/MviViewModel expose ViewModel as their
            // public supertype, so it must be visible on consumers' compile classpath.
            api(libs.androidx.lifecycle.viewmodel)
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}
