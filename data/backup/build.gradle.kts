import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    id("physioapp.kmp.base")
}

kotlin {
    // Same libsqlite3 linker requirement as :data:auth (see core/database/build.gradle.kts).
    targets.withType<KotlinNativeTarget>().configureEach {
        binaries.withType<Framework>().configureEach {
            linkerOpts += "-lsqlite3"
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            // The file name carries the device's local date and time.
            implementation(libs.kotlinx.datetime)
            implementation(projects.domain.backup)
            implementation(projects.core.database)
            // The date of the last backup lives in the OS secure storage, next to the session.
            implementation(projects.core.storage)
            implementation(libs.koin.core)
        }
    }
}
