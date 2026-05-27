package io.github.ackeecz.apythia.plugin

import com.android.build.api.dsl.CommonExtension
import io.github.ackeecz.apythia.util.Constants
import org.gradle.api.Plugin
import org.gradle.api.Project

internal class AndroidPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.configure()
    }

    private fun Project.configure() {
        androidCommon {
            configureSdkVersions()
            configureCompileOptions()
        }
    }

    private fun CommonExtension.configureSdkVersions() {
        compileSdk = Constants.COMPILE_SDK
        defaultConfig.minSdk = Constants.MIN_SDK
    }

    private fun CommonExtension.configureCompileOptions() {
        compileOptions.sourceCompatibility = Constants.JAVA_VERSION
        compileOptions.targetCompatibility = Constants.JAVA_VERSION
    }
}
