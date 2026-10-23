package com.connectapp.domain.provider

/**
 * The app's display version name (e.g. "1.0"), resolved at build time from the release pipeline
 * (specs/012-release-pipeline-automation) — see composeApp/build.gradle.kts' `generateAppConfig`
 * task. `:domain` only declares the seam; `:composeApp` is the only module that knows how the
 * value is actually produced.
 */
interface AppVersionProvider {
    val versionName: String
}
