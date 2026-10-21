plugins {
    // Gives the root project its own `check`/`build` lifecycle tasks (it has none by
    // default — only subprojects get them, via the Android/Kotlin plugins each applies) so
    // verifyModuleBoundaries (specs/013-enforce-module-boundaries) has a root `check` to
    // hook into.
    id("base")
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.androidKotlinMultiplatformLibrary) apply false
    alias(libs.plugins.androidLint) apply false
    alias(libs.plugins.detekt) apply false
    // specs/013-enforce-module-boundaries — applied here only (root project), not `apply false`.
    id("physioapp.module-boundaries")
    // specs/014-code-coverage-tooling — applied here only (root project). Modules are added
    // as `kover(project(":..."))` dependencies below, never by applying this plugin in a
    // module's own build.gradle.kts (contracts/kover-config-contract.md).
    alias(libs.plugins.kover)
}

// research.md D4: code we don't write (Compose Resources accessors, SQLDelight generated
// query/database classes) is excluded so the coverage percentage reflects only hand-written
// production code. Kept centralized here instead of per-module to keep this the single
// point of truth for coverage config, same rationale as config/detekt/detekt.yml.
kover {
    reports {
        filters {
            excludes {
                classes(
                    "*.generated.resources.*",
                    "com.connectapp.commonresources.resources.*",
                    "com.connectapp.data.database.*",
                )
            }
        }
    }
}

// A module not listed here simply doesn't appear in the aggregated report — there is no
// convention plugin that adds this automatically (research.md D2), so a new Gradle module
// needs a line added here as part of its "new module" checklist, same as its detekt
// baseline. build-logic is deliberately absent: it's a separate Gradle build with no test
// code (research.md D3).
dependencies {
    kover(project(":domain:auth"))
    kover(project(":domain:settings"))
    kover(project(":domain:shared"))
    kover(project(":presentation"))
    kover(project(":commonResources"))
    kover(project(":composeApp"))
    kover(project(":core:storage"))
    kover(project(":core:database"))
    kover(project(":core:mvi"))
    kover(project(":core:crypto"))
    kover(project(":core:pdf"))
    kover(project(":domain:professionals"))
    kover(project(":domain:patients"))
    kover(project(":domain:reports"))
    kover(project(":domain:invoices"))
    kover(project(":data:professionals"))
    kover(project(":data:patients"))
    kover(project(":data:reports"))
    kover(project(":data:invoices"))
    kover(project(":data:auth"))
    kover(project(":feature:splash"))
    kover(project(":feature:login"))
    kover(project(":feature:register"))
    kover(project(":feature:onboarding"))
    kover(project(":feature:forgot_password"))
    kover(project(":feature:home"))
    kover(project(":feature:settings"))
    kover(project(":feature:profile"))
    kover(project(":feature:professionals"))
    kover(project(":feature:patients"))
    kover(project(":feature:reports"))
    kover(project(":feature:invoices"))
}