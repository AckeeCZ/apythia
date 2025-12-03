package io.github.ackeecz.apythia.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType

/**
 * Extends [KotlinJvmLibraryPlugin] with testing logic. If you need Kotlin JVM library with tests,
 * apply this instead of [KotlinJvmLibraryPlugin]. These Kotlin JVM plugins follow the same structure
 * as KMP plugins. For more info about this see [KmpLibraryWithTestingPlugin].
 */
internal class KotlinJvmLibraryWithTestingPlugin : Plugin<Project> {

    private val kotlinJvmLibraryPlugin = KotlinJvmLibraryPlugin()

    override fun apply(target: Project) = with(target) {
        kotlinJvmLibraryPlugin.applyPlugins(project)
        applyPlugins()
        kotlinJvmLibraryPlugin.configurePlugins(project)
        configurePlugins()
    }

    private fun Project.applyPlugins() {
        pluginManager.apply(libs.plugins.gradle.testLogger)
    }

    private fun Project.configurePlugins() {
        tasks.withType<Test>().configureEach {
            useJUnitPlatform()
        }

        dependencies {
            testImplementation(libs.kotest.assertions.core)
            testImplementation(libs.kotest.runner.junit5)
        }
    }
}
