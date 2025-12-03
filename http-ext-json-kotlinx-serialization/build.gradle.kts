import io.github.ackeecz.apythia.util.Constants

plugins {
    alias(libs.plugins.apythia.kotlin.multiplatform.librarywithtesting)
    alias(libs.plugins.apythia.publishing)
}

kotlin {

    androidLibrary {
        namespace = "${Constants.NAMESPACE_PREFIX}.http.extension.json.kotlinx.serialization"
    }

    sourceSets {
        commonMain {
            dependencies {
                api(projects.http)
                api(libs.kotlin.serialization.json)
                implementation(libs.kotest.assertions.core)
            }
        }

        commonTest {
            dependencies {
                implementation(projects.httpTesting)
            }
        }
    }
}
