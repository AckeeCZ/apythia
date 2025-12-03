package io.github.ackeecz.apythia.plugin

import io.github.ackeecz.apythia.util.Constants
import org.jetbrains.kotlin.gradle.dsl.KotlinCommonCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.dsl.abi.AbiValidationExtension
import org.jetbrains.kotlin.gradle.dsl.abi.AbiValidationVariantSpec
import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation

internal fun KotlinJvmCompilerOptions.configureAllOptions() {
    configureCommonOptions()
    configureJvmSpecificOptions()
}

internal fun KotlinCommonCompilerOptions.configureCommonOptions() {
    allWarningsAsErrors.set(true)
    freeCompilerArgs.add(
        "-Xconsistent-data-class-copy-visibility"
    )
}

internal fun KotlinJvmCompilerOptions.configureJvmSpecificOptions() {
    jvmTarget.set(Constants.JVM_TARGET)
}

internal fun KotlinProjectExtension.commonConfiguration() {
    explicitApi()
}
