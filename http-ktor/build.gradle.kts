import io.github.ackeecz.apythia.util.Constants

plugins {
    alias(libs.plugins.apythia.kotlin.multiplatform.librarywithtesting)
    alias(libs.plugins.apythia.publishing)
}

kotlin {

    androidLibrary {
        namespace = "${Constants.NAMESPACE_PREFIX}.http.ktor"
    }

    sourceSets {
        commonMain {
            dependencies {
                api(projects.http)

                api(libs.ktor.mock)
            }
        }

        commonTest {
            dependencies {
                implementation(projects.httpTesting)

                implementation(libs.ktor.contentNegotiation)
                implementation(libs.ktor.logging)
                implementation(libs.ktor.serialization.json)
            }
        }
    }
}
