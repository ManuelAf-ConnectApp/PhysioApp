import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    id("physioapp.kmp.base")
}

kotlin {
    // See core/database/build.gradle.kts — same libsqlite3 linker requirement, needed here too
    // since this module's own iOS framework links app.cash.sqldelight's native-driver through
    // :core:database. The shipped app links SQLCipher instead.
    targets.withType<KotlinNativeTarget>().configureEach {
        binaries.withType<Framework>().configureEach {
            linkerOpts += "-lsqlite3"
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.sqldelight.coroutines.extensions)
            // api (not implementation): KVaultSessionStore's public constructor takes a KVault.
            api(libs.kvault)
            implementation(projects.domain.auth)
            implementation(projects.core.database)
            implementation(projects.core.crypto)
            implementation(projects.core.storage)
            implementation(libs.koin.core)
        }

        // Repository tests run against the real PhysioDatabase schema on the JVM (plain SQLite
        // through JDBC; encryption is covered by :core:database's device tests).
        getByName("androidHostTest").dependencies {
            implementation(libs.sqldelight.sqlite.driver)
        }
    }
}
