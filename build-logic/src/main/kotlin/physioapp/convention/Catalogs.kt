package physioapp.convention

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

/**
 * The generated `libs.x.y` accessor sugar only works in an ordinary
 * build.gradle.kts — precompiled script plugins (compiled ahead of time as
 * part of build-logic's own sources) must go through [VersionCatalogsExtension]
 * directly. These helpers keep that lookup terse in the convention plugins.
 */
internal fun Project.libsCatalog() =
    extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun Project.library(alias: String) =
    libsCatalog().findLibrary(alias).get()

internal fun Project.catalogVersion(alias: String): String =
    libsCatalog().findVersion(alias).get().requiredVersion
