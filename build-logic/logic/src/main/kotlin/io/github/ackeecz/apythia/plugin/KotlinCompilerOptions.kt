package io.github.ackeecz.apythia.plugin

import io.github.ackeecz.apythia.util.Constants
import org.jetbrains.kotlin.gradle.dsl.KotlinCommonCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions

internal fun KotlinJvmCompilerOptions.configureApplicationOptions() {
    configureCommonOptions()
    configureLibraryOptions()
}

internal fun KotlinJvmCompilerOptions.configureLibraryOptions() {
    jvmTarget.set(Constants.JVM_TARGET)
}

internal fun KotlinCommonCompilerOptions.configureCommonOptions() {
    allWarningsAsErrors.set(true)
}
