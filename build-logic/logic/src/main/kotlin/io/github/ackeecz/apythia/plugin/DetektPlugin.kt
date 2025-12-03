package io.github.ackeecz.apythia.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.kotlin.dsl.creating
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getValue

internal class DetektPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.configure()
    }

    private fun Project.configure() {
        pluginManager.apply(libs.plugins.detekt)

        val detektConfig: Configuration by configurations.creating {}

        detekt {
            buildUponDefaultConfig = true
            config.setFrom(provider { detektConfig.files })
            ignoreFailures = false
        }

        dependencies {
            detektConfig(libs.ackee.detekt.config.core)
            detektPlugins(libs.detekt.formatting)
        }
    }
}
