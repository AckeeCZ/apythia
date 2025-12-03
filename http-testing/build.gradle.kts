import io.github.ackeecz.apythia.util.Constants

plugins {
    alias(libs.plugins.apythia.kotlin.multiplatform.library)
}

kotlin {

    androidLibrary {
        namespace = "${Constants.NAMESPACE_PREFIX}.http.testing"
    }

    sourceSets.commonMain.dependencies {
        implementation(projects.http)

        implementation(libs.kotest.assertions.core)
        implementation(libs.kotest.framework.engine)
    }
}

kmpLibrary {
    abiValidationEnabled.set(false)
}
