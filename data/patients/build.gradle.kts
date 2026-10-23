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
            implementation(libs.sqldelight.coroutines.extensions)
            implementation(projects.domain.patients)
            implementation(projects.core.database)
            implementation(libs.koin.core)
        }

        // Repository tests run against the real PhysioDatabase schema on the JVM.
        getByName("androidHostTest").dependencies {
            implementation(libs.sqldelight.sqlite.driver)
        }
    }
}
