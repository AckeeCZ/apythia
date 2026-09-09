package io.github.ackeecz.apythia.plugin

import io.github.ackeecz.apythia.util.Constants
import org.gradle.api.Plugin
import org.gradle.api.Project

internal class AndroidApplicationPlugin : Plugin<Project> {

    private val androidPlugin = AndroidPlugin()
    private val detektPlugin = DetektPlugin()

    override fun apply(target: Project) {
        target.configure()
        target.configureKotlin()
        androidPlugin.apply(target)
        detektPlugin.apply(target)
    }

    private fun Project.configure() {
        pluginManager.apply(libs.plugins.android.application)

        androidApplication {

            defaultConfig {
                targetSdk = Constants.TARGET_SDK
                versionCode = 1
                versionName = "1.0"
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            }

            buildTypes {
                release {
                    optimization {
                        // Enables code optimization together with optimized resource shrinking
                        enable = true

                        keepRules {
                            // The default Android keep rules are included automatically, so only
                            // optional custom rules of the module need to be added here.
                            file("proguard-rules.pro")
                                .takeIf { it.exists() }
                                ?.let { files.add(it) }
                        }
                    }
                }
            }

            testOptions {
                unitTests.all {
                    it.useJUnitPlatform()
                }
            }
        }
    }

    private fun Project.configureKotlin() {
        kotlinAndroid {
            compilerOptions {
                configureAllOptions()
            }
        }
    }
}
