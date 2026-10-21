rootProject.name = "PhysioApp"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("build-logic")

    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

include(":composeApp")
include(":domain:auth")
include(":domain:settings")
include(":domain:shared")
include(":domain:professionals")
include(":domain:patients")
include(":domain:reports")
include(":domain:invoices")
include(":domain:backup")
include(":presentation")
include(":commonResources")
include(":core:storage")
include(":core:database")
include(":core:mvi")
include(":core:crypto")
include(":core:pdf")
include(":core:uitest")
include(":data:auth")
include(":data:professionals")
include(":data:patients")
include(":data:reports")
include(":data:invoices")
include(":data:backup")
include(":feature:splash")
include(":feature:login")
include(":feature:register")
include(":feature:onboarding")
include(":feature:forgot_password")
include(":feature:home")
include(":feature:settings")
include(":feature:profile")
include(":feature:professionals")
include(":feature:patients")
include(":feature:reports")
include(":feature:invoices")
include(":feature:backup")
