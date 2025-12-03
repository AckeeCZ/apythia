import io.github.ackeecz.apythia.util.Constants

plugins {
    alias(libs.plugins.apythia.kotlin.multiplatform.librarywithtesting)
    alias(libs.plugins.apythia.publishing)
}

kotlin {

    androidLibrary {
        namespace = "${Constants.NAMESPACE_PREFIX}.http"
    }

    sourceSets {
        commonMain {
            dependencies {
                // TODO Extract Kotlin Serialization to a separate module
                api(libs.kotlin.serialization.json)
                implementation(libs.kmpUri)
                implementation(libs.kotest.assertions.core)
            }
        }
    }
}
