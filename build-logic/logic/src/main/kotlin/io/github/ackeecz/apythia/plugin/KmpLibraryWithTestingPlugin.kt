package io.github.ackeecz.apythia.plugin

import com.android.build.api.dsl.androidLibrary
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.withType

/**
 * Extends [KmpLibraryPlugin] with KMP testing logic. If you need KMP library with tests, apply this
 * instead of [KmpLibraryPlugin].
 */
internal class KmpLibraryWithTestingPlugin : Plugin<Project> {

    private val kmpLibraryPlugin = KmpLibraryPlugin()

    override fun apply(target: Project) = with(target) {
        // We need to first apply all KMP plugins and then configure logic for both KmpLibraryPlugin
        // and this testing one. There is an issue with combination of
        // com.android.kotlin.multiplatform.library plugin, JVM target and io.kotest plugin.
        // When io.kotest plugin is applied after com.android.kotlin.multiplatform.library
        // and JVM target were configured, it fails with a weird already existing jvmKotest task error.
        // So we need to apply all KMP plugins first and then perform all configurations. For this
        // reason we can't have two completely separate KmpLibraryPlugin and KmpTestingPlugin
        // plugins.
        kmpLibraryPlugin.applyPlugins(project)
        applyPlugins()
        kmpLibraryPlugin.configurePlugins(project)
        configurePlugins()
    }

    private fun Project.applyPlugins() {
        pluginManager.apply(libs.plugins.kotlin.ksp)
        pluginManager.apply(libs.plugins.kotest.multiplatform)
        pluginManager.apply(libs.plugins.gradle.testLogger)
    }

    @Suppress("UnstableApiUsage")
    private fun Project.configurePlugins() {
        kotlinMultiplatform {
            androidLibrary {
                // This nice config enables Android host (local unit) tests in KMP module 🫠
                withHostTestBuilder {}.configure {}
            }

            sourceSets.commonTest.dependencies {
                // Kotest
                implementation(libs.kotest.assertions.core)
                implementation(libs.kotest.framework.engine)
            }

            sourceSets.androidHostTest.dependencies {
                implementation(dependencies.platform(libs.junit.bom))
                implementation(libs.kotest.runner.junit5)
            }

            sourceSets.jvmTest.dependencies {
                implementation(dependencies.platform(libs.junit.bom))
                implementation(libs.kotest.runner.junit5)
            }
        }

        tasks.withType<Test> {
            useJUnitPlatform()
        }
    }
}
