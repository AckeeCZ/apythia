import io.github.ackeecz.apythia.util.Constants

plugins {
    alias(libs.plugins.ackeecz.apythia.kmp.library)
    alias(libs.plugins.ackeecz.apythia.kmp.testing)
    alias(libs.plugins.ackeecz.apythia.publishing)
}

kotlin {

    androidLibrary {
        namespace = "${Constants.NAMESPACE_PREFIX}.http"
    }
}
