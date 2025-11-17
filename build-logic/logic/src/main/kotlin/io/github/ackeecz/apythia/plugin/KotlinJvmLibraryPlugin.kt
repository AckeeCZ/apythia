package io.github.ackeecz.apythia.plugin

import io.github.ackeecz.apythia.util.Constants
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation

internal class KotlinJvmLibraryPlugin : Plugin<Project> {

    private val detektPlugin = DetektPlugin()

    override fun apply(target: Project) {
        applyPlugins(target)
        configurePlugins(target)
    }

    fun applyPlugins(target: Project) = with(target) {
        target.pluginManager.apply("java-library")
        target.pluginManager.apply(libs.plugins.kotlin.jvm)
        detektPlugin.apply(this)
    }

    @OptIn(ExperimentalAbiValidation::class)
    fun configurePlugins(target: Project) = with(target) {
        java {
            sourceCompatibility = Constants.JAVA_VERSION
            targetCompatibility = Constants.JAVA_VERSION
        }

        kotlinJvm {
            commonConfiguration()
            compilerOptions {
                configureAllOptions()
            }
            abiValidation {
                enabled.set(true)
            }
        }
    }
}
