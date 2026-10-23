plugins {
    `kotlin-dsl`
    alias(libs.plugins.detekt)
}

repositories {
    google()
    mavenCentral()
}

dependencies {
    // Plugin marker artifacts: lets the precompiled script plugins in
    // src/main/kotlin apply these plugins by id.
    implementation("org.jetbrains.kotlin.multiplatform:org.jetbrains.kotlin.multiplatform.gradle.plugin:${libs.versions.kotlin.get()}")
    implementation("org.jetbrains.kotlin.plugin.compose:org.jetbrains.kotlin.plugin.compose.gradle.plugin:${libs.versions.kotlin.get()}")
    implementation("com.android.kotlin.multiplatform.library:com.android.kotlin.multiplatform.library.gradle.plugin:${libs.versions.agp.get()}")
    implementation("com.android.lint:com.android.lint.gradle.plugin:${libs.versions.agp.get()}")
    implementation("org.jetbrains.compose:org.jetbrains.compose.gradle.plugin:${libs.versions.composeMultiplatform.get()}")
    implementation("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:${libs.versions.detekt.get()}")
    // specs/014-code-coverage-tooling — lets physioapp.kmp.base.gradle.kts apply Kover by id.
    implementation("org.jetbrains.kotlinx.kover:org.jetbrains.kotlinx.kover.gradle.plugin:${libs.versions.kover.get()}")

    detektPlugins(libs.detekt.formatting)
}

// build-logic can't depend on physioapp.kmp.base, which lives inside itself -- see
// specs/005-static-analysis-ci/plan.md § Complexity Tracking.
detekt {
    config.setFrom(file("../config/detekt/detekt.yml"))
    baseline = file("detekt-baseline.xml")
    buildUponDefaultConfig = true
}
