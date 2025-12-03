import io.github.ackeecz.apythia.properties.LibraryProperties
import io.github.ackeecz.apythia.util.Constants

repositories {
    google()
    mavenLocal()
    mavenCentral()
}

plugins {
    alias(libs.plugins.apythia.android.application)
}

android {
    namespace = "${Constants.NAMESPACE_PREFIX}.sample"

    defaultConfig {
        applicationId = Constants.NAMESPACE_PREFIX
    }
}

@Suppress("UseTomlInstead")
dependencies {

    val bomVersion = LibraryProperties(project).bomArtifactProperties.version
    testImplementation(platform("io.github.ackeecz:apythia-bom:$bomVersion"))
    testImplementation("io.github.ackeecz:apythia-http-ext-json-kotlinx-serialization")
    testImplementation("io.github.ackeecz:apythia-http-ktor")
    testImplementation("io.github.ackeecz:apythia-http-okhttp")

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.kotest.runner.junit5)
}
