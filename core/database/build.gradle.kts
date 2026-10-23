import java.net.URI
import java.security.MessageDigest
import javax.inject.Inject
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.TestExecutable

/**
 * Downloads the official SQLCipher.xcframework (the same artifact iosApp's Swift Package uses)
 * and verifies its SHA-256, so iosSimulatorArm64Test can link the real SQLCipher and prove at
 * runtime that the iOS database is encrypted.
 */
abstract class FetchSqlCipherXcframework : DefaultTask() {
    @get:Input
    abstract val url: Property<String>

    @get:Input
    abstract val sha256: Property<String>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @get:Inject
    abstract val archives: ArchiveOperations

    @get:Inject
    abstract val files: FileSystemOperations

    @TaskAction
    fun fetch() {
        val zip = temporaryDir.resolve("SQLCipher.xcframework.zip")
        URI(url.get()).toURL().openStream().use { input -> zip.outputStream().use { input.copyTo(it) } }
        val actual = MessageDigest.getInstance("SHA-256").digest(zip.readBytes())
            .joinToString("") { "%02x".format(it) }
        check(actual == sha256.get()) { "SQLCipher.xcframework checksum mismatch: $actual" }
        files.sync {
            from(archives.zipTree(zip))
            into(outputDir)
        }
    }
}

plugins {
    id("physioapp.kmp.base")
    alias(libs.plugins.sqldelight)
}

kotlin {
    // SQLDelight's native-driver (touchlab/sqliter) links against the system libsqlite3 dylib;
    // Kotlin/Native doesn't add it automatically when this module's own iOS framework is linked
    // standalone (as every module here does, per physioapp.kmp.base.gradle.kts) — without this,
    // linkDebugFrameworkIosArm64/-SimulatorArm64 fail with "Undefined symbols: _sqlite3_bind_*".
    // These standalone frameworks are only a compile check: the shipped app links SQLCipher
    // instead (iosApp, Swift Package "SQLCipher"), and openPhysioDatabase() refuses to run on a
    // non-SQLCipher build.
    targets.withType<KotlinNativeTarget>().configureEach {
        binaries.withType<Framework>().configureEach {
            linkerOpts += "-lsqlite3"
        }
    }

    sourceSets {
        commonMain.dependencies {
            // api (not implementation): createSqlDriver()/openPhysioDatabase() return these
            // types directly to callers.
            api(libs.sqldelight.runtime)
        }

        androidMain.dependencies {
            implementation(libs.sqldelight.android.driver)
            // docs/00-auditoria-y-plan.md D7 (confirmed 2026-10-16): encrypted database.
            implementation(libs.sqlcipher.android)
        }

        iosMain.dependencies {
            implementation(libs.sqldelight.native.driver)
        }

        // Schema/constraint tests run on the JVM with SQLDelight's JDBC driver (plain SQLite):
        // they check the data model, not the encryption, which androidDeviceTest covers.
        getByName("androidHostTest").dependencies {
            implementation(libs.sqldelight.sqlite.driver)
        }
    }
}

sqldelight {
    databases {
        create("PhysioDatabase") {
            packageName.set("com.connectapp.core.database.physio")
            // Committed schema snapshots (src/commonMain/sqldelight/databases/<version>.db) let
            // verifySqlDelightMigration prove every future .sqm migration reaches the same schema.
            schemaOutputDirectory.set(file("src/commonMain/sqldelight/databases"))
            verifyMigrations.set(true)
        }
    }
}

// Keep in sync with gradle/libs.versions.toml (sqlcipher) and iosApp's Swift Package pin. The
// checksum is the one published in SQLCipher.swift's Package.swift for that version.
val fetchSqlCipherXcframework = tasks.register<FetchSqlCipherXcframework>("fetchSqlCipherXcframework") {
    val version = libs.versions.sqlcipher.get()
    url.set("https://github.com/sqlcipher/SQLCipher.swift/releases/download/$version/SQLCipher.xcframework.zip")
    sha256.set("dd5a650346c1ba9933d6ba179f8844e03e4a075b3dd3a892796149864cd9ae57")
    outputDir.set(layout.buildDirectory.dir("sqlcipher/$version"))
}

kotlin.targets.withType<KotlinNativeTarget>().matching { it.name == "iosSimulatorArm64" }.configureEach {
    binaries.withType<TestExecutable>().configureEach {
        val frameworkDir = fetchSqlCipherXcframework.get().outputDir.get()
            .dir("SQLCipher.xcframework/ios-arm64_x86_64-simulator").asFile.absolutePath
        linkerOpts("-F$frameworkDir", "-framework", "SQLCipher", "-rpath", frameworkDir)
        linkTaskProvider.configure { dependsOn(fetchSqlCipherXcframework) }
    }
}
