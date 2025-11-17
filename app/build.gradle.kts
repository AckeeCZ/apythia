import io.github.ackeecz.apythia.properties.LibraryProperties
import io.github.ackeecz.apythia.util.Constants

repositories {
    google()
    mavenLocal()
    mavenCentral()
}

plugins {
    alias(libs.plugins.ackeecz.apythia.android.application)
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
    implementation(platform("io.github.ackeecz:apythia-bom:$bomVersion"))
    implementation("io.github.ackeecz:apythia-http")
}
