package com.connectapp.presentation.settings.licenses

/**
 * Third-party software shipped in PhysioApp, grouped by licence text (docs/licencias-terceros.md,
 * 2026-10-21). MIT and BSD-3-Clause require their notice to accompany every binary, which is
 * why this screen exists. Update it together with the inventory and NOTICE whenever a runtime
 * dependency is added or changes licence.
 */
internal data class LicenseGroup(
    /** Licence name as published; not translated. */
    val title: String,
    /** File under composeResources/files/licenses/ in :commonResources, or null for public domain. */
    val textFile: String?,
    val components: List<OpenSourceComponent>,
)

internal data class OpenSourceComponent(
    val name: String,
    val holder: String,
    /** Only linked into the iOS app (e.g. Skia, Compose's renderer there). */
    val iPadOnly: Boolean = false,
)

internal val openSourceLicenses: List<LicenseGroup> = listOf(
    LicenseGroup(
        title = "Apache License 2.0",
        textFile = "apache-2.0.txt",
        components = listOf(
            OpenSourceComponent(
                name = "AndroidX (Activity, Core, Lifecycle, Navigation3, SQLite, Security…)",
                holder = "Google LLC",
            ),
            OpenSourceComponent(
                name = "Jetpack Compose / Compose Multiplatform, Material 3, Material Icons",
                holder = "Google LLC, JetBrains s.r.o.",
            ),
            OpenSourceComponent(
                name = "Kotlin, kotlinx.coroutines, kotlinx.datetime, kotlinx.serialization",
                holder = "JetBrains s.r.o.",
            ),
            OpenSourceComponent("Skiko", "JetBrains s.r.o.", iPadOnly = true),
            OpenSourceComponent("Koin", "Kotzilla"),
            OpenSourceComponent("SQLDelight", "Square, Inc."),
            OpenSourceComponent("SQLiter, Stately", "Touchlab"),
            OpenSourceComponent("Tink", "Google LLC"),
            OpenSourceComponent("Gson", "Google LLC"),
            OpenSourceComponent("Guava listenablefuture", "The Guava Authors"),
            OpenSourceComponent("JSpecify", "The JSpecify Authors"),
        ),
    ),
    LicenseGroup(
        title = "MIT License",
        textFile = "mit-kvault.txt",
        components = listOf(OpenSourceComponent("KVault", "Liftric GmbH")),
    ),
    LicenseGroup(
        title = "BSD 3-Clause License",
        textFile = "bsd-3-clause-sqlcipher.txt",
        components = listOf(OpenSourceComponent("SQLCipher Community Edition", "Zetetic LLC")),
    ),
    LicenseGroup(
        title = "BSD 3-Clause License",
        textFile = "bsd-3-clause-skia.txt",
        components = listOf(OpenSourceComponent("Skia", "Google Inc.", iPadOnly = true)),
    ),
    LicenseGroup(
        title = "SQLite",
        textFile = null,
        components = listOf(OpenSourceComponent("SQLite", "D. Richard Hipp and contributors")),
    ),
)
