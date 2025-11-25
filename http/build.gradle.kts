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
                implementation(libs.kmpUri)
                implementation(libs.kotest.assertions.core)
            }
        }
    }
}
