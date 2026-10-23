import physioapp.convention.catalogVersion
import physioapp.convention.library

// Common skeleton shared by every KMP module of this template: Android + iOS targets,
// host/device test builders and the base commonMain/commonTest dependencies.
//
// Namespace and iOS framework name follow this template's module layout, so no module
// needs to redeclare them: feature/<name> modules become com.connectapp.presentation.<name>,
// core/<name> modules become com.connectapp.core.<name>, data/<name> modules become
// com.connectapp.data.<name> (except data/auth, grandfathered to com.connectapp.data --
// its Kotlin package predates the data/* grouping and wasn't renamed when the module
// moved from :data to :data:auth), domain/<name> modules become com.connectapp.domain.<name>
// as a namespace only -- their Kotlin package stays the flat, pre-split com.connectapp.domain
// (same rationale as data/auth: :domain existed as a single module long before being split
// by bounded context, and namespace/package are independent concerns for a module with no
// Android resources of its own), everything else becomes
// com.connectapp.<module name, lowercase>; the iOS framework is always
// <moduleNameCamelCase>Kit. AGP reads namespace/compileSdk the moment android { } is
// first configured, so these can't be supplied later via an extension block -- deriving
// them from the module's own coordinates avoids that ordering problem entirely instead
// of fighting it.
//
// NOTE: keep this file's plugins{} block preceded only by line comments (//), not a
// block/KDoc comment (/** */). A block comment right before plugins{} in this specific
// file makes the Kotlin compiler (2.3.21, as used by this project) silently emit an
// empty class body for this precompiled script plugin -- no compile error, but the
// plugin then applies as a no-op (no Android/iOS targets, no KotlinMultiplatformExtension
// registered). Reproduced by toggling only the comment style with everything else held
// constant; root cause not otherwise diagnosed. Consider filing/searching a Kotlin or
// Gradle issue before touching this again.
plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id("com.android.lint")
    id("io.gitlab.arturbosch.detekt")
    // specs/014-code-coverage-tooling: Kover must be applied on every module that the root
    // project's `kover(project(":..."))` dependency references (research.md D2 — the root-only
    // application first tried doesn't publish the variant Gradle needs to resolve that
    // dependency), so it's applied here rather than once at the root.
    id("org.jetbrains.kotlinx.kover")
}

val physioAppNamespace = when {
    project.path.startsWith(":feature:") -> "com.connectapp.presentation.${project.name}"
    project.path.startsWith(":core:") -> "com.connectapp.core.${project.name}"
    project.path == ":data:auth" -> "com.connectapp.data"
    project.path.startsWith(":data:") -> "com.connectapp.data.${project.name}"
    project.path.startsWith(":domain:") -> "com.connectapp.domain.${project.name}"
    else -> "com.connectapp.${project.name.lowercase()}"
}

val physioAppXcfName = project.name
    .split("_")
    .mapIndexed { index, part -> if (index == 0) part else part.replaceFirstChar(Char::uppercase) }
    .joinToString("") + "Kit"

kotlin {
    android {
        namespace = physioAppNamespace
        compileSdk = catalogVersion("android-compileSdk").toInt()
        minSdk = catalogVersion("android-minSdk").toInt()

        // Android resources in host tests: Compose UI tests run there under Robolectric.
        withHostTestBuilder {
        }.configure {
            isIncludeAndroidResources = true
        }

        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach {
        it.binaries.framework {
            baseName = physioAppXcfName
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(library("kotlin-stdlib"))
        }

        commonTest.dependencies {
            implementation(library("kotlin-test"))
            implementation(library("kotlinx-coroutines-test"))
        }

        getByName("androidDeviceTest") {
            dependencies {
                implementation(library("androidx-runner"))
                implementation(library("androidx-core"))
                implementation(library("androidx-testExt-junit"))
            }
        }
    }
}

// See specs/005-static-analysis-ci/contracts/detekt-config-contract.md: same shared config
// file and buildUponDefaultConfig everywhere, baseline kept local to each module.
//
// `source` is set explicitly because this is a Kotlin Multiplatform module: with no
// classic single Java/Kotlin "main" source set, the plain `detekt`/`detektBaseline`
// tasks default to analyzing nothing (silent NO-SOURCE) unless told what to scan. Setting
// it to just the hand-written source dirs also keeps generated code (e.g. Compose
// Resources accessors under build/generated/...) out of the baseline -- its churny,
// human-uneditable findings would otherwise get baked in permanently. (The KMP plugin
// additionally auto-generates one analysis task per target/source-set, e.g.
// detektMetadataCommonMain/detektAndroidMain/detektIosArm64Main -- those ignore this
// `source` override and always scan the full compilation input including generated code,
// so they're deliberately left unused here in favor of this single, filterable task.)
detekt {
    config.setFrom(rootProject.file("config/detekt/detekt.yml"))
    baseline = project.file("detekt-baseline.xml")
    buildUponDefaultConfig = true
    source.setFrom(
        files("src/commonMain/kotlin", "src/androidMain/kotlin", "src/iosMain/kotlin")
    )
}

dependencies {
    detektPlugins(library("detekt-formatting"))
}
