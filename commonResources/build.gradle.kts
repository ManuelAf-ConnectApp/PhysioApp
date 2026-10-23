import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.kotlin.dsl.withType

plugins {
    id("physioapp.kmp.compose")
}

kotlin {
    targets.withType<KotlinMultiplatformAndroidLibraryTarget>().configureEach {
        androidResources {
            enable = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            // Password visibility icons of the shared ValidatedTextField.
            implementation(libs.compose.icons.extended)
            // Dates of signatures and reports in the device's time zone.
            api(libs.kotlinx.datetime)
        }
        androidMain.dependencies {
            // Share sheet and "save to" picker of exported PDFs (D19).
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.core.ktx)
        }
    }
}

compose.resources {
    packageOfResClass = "com.connectapp.commonresources.resources"
    generateResClass = always
    publicResClass = true
}
