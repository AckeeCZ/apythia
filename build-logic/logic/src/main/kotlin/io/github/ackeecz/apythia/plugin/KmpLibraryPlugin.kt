package io.github.ackeecz.apythia.plugin

import com.android.build.api.dsl.androidLibrary
import io.github.ackeecz.apythia.util.Constants
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.create
import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation

internal class KmpLibraryPlugin : Plugin<Project> {

    private val detektPlugin = DetektPlugin()

    override fun apply(target: Project) {
        applyPlugins(target)
        configurePlugins(target)
    }

    fun applyPlugins(target: Project) = with(target) {
        pluginManager.apply(libs.plugins.kotlin.multiplatform)
        pluginManager.apply(libs.plugins.android.kmp.library)
    }

    @Suppress("UnstableApiUsage")
    fun configurePlugins(target: Project) = with(target) {
        val extension = createExtension()

        kotlinMultiplatform {
            commonConfiguration()

            compilerOptions {
                configureCommonOptions()
            }

            @OptIn(ExperimentalAbiValidation::class)
            abiValidation {
                enabled.set(extension.abiValidationEnabled)
                klib {
                    enabled.set(true)
                    keepUnsupportedTargets.set(false)
                }
            }

            androidLibrary {
                compileSdk = Constants.COMPILE_SDK
                minSdk = Constants.MIN_SDK

                compilations.configureEach {
                    compileTaskProvider.configure {
                        compilerOptions.configureJvmSpecificOptions()
                    }
                }

                optimization {
                    consumerKeepRules.publish = true
                    consumerKeepRules.file("consumer-rules.pro")
                    minify = false
                }
            }

            val xcfName = "${target.name}Kit"
            iosX64 {
                binaries.framework {
                    baseName = xcfName
                }
            }
            iosArm64 {
                binaries.framework {
                    baseName = xcfName
                }
            }
            iosSimulatorArm64 {
                binaries.framework {
                    baseName = xcfName
                }
            }

            jvm {
                compilations.configureEach {
                    compileTaskProvider.configure {
                        compilerOptions.configureJvmSpecificOptions()
                    }
                }
            }
        }

        configureDetekt()
    }

    private fun Project.createExtension(): KmpLibraryExtension {
        return extensions.create<KmpLibraryExtension>("kmpLibrary").also {
            it.abiValidationEnabled.convention(true)
        }
    }

    private fun Project.configureDetekt() {
        detektPlugin.apply(this)
        addAllKmpSourceSetsToDetekt()
    }

    private fun Project.addAllKmpSourceSetsToDetekt() {
        val allKmpSources = file("${project.projectDir.absolutePath}/src")
            .listFiles()
            .filter { it.isDirectory }
            .map { "src/${it.name}/kotlin" }
            .let { files(it) }

        detekt {
            source.setFrom(allKmpSources)
        }
    }
}

public interface KmpLibraryExtension {

    public val abiValidationEnabled: Property<Boolean>
}
