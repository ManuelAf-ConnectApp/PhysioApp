plugins {
    id("physioapp.kmp.base")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // api (not implementation): createKVault() returns this type directly to callers.
            api(libs.kvault)
        }
    }
}
