import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
        freeCompilerArgs.add("-Xexplicit-api=strict")
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

dependencies {
    compileOnly(files(libs::class.java.superclass.protectionDomain.codeSource.location))
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.detekt.gradlePlugin)
    compileOnly(libs.gradle.versions.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.mavenPublish.gradlePlugin)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kotest.runner.junit5)
}

gradlePlugin {
    plugins {
        plugin(
            dependency = libs.plugins.apythia.android.application,
            pluginClassSimpleName = "AndroidApplicationPlugin",
        )

        plugin(
            dependency = libs.plugins.apythia.dependency.updates,
            pluginClassSimpleName = "DependencyUpdatesPlugin",
        )

        plugin(
            dependency = libs.plugins.apythia.kotlin.multiplatform.library,
            pluginClassSimpleName = "KmpLibraryPlugin",
        )

        plugin(
            dependency = libs.plugins.apythia.kotlin.multiplatform.librarywithtesting,
            pluginClassSimpleName = "KmpLibraryWithTestingPlugin",
        )

        plugin(
            dependency = libs.plugins.apythia.kotlin.jvm.library,
            pluginClassSimpleName = "KotlinJvmLibraryPlugin",
        )

        plugin(
            dependency = libs.plugins.apythia.kotlin.jvm.librarywithtesting,
            pluginClassSimpleName = "KotlinJvmLibraryWithTestingPlugin",
        )

        plugin(
            dependency = libs.plugins.apythia.publishing,
            pluginClassSimpleName = "PublishingPlugin",
        )

        plugin(
            dependency = libs.plugins.apythia.preflightchecks,
            pluginClassSimpleName = "RegisterPreflightChecksPlugin",
        )
    }
}

private fun NamedDomainObjectContainer<PluginDeclaration>.plugin(
    dependency: Provider<out PluginDependency>,
    pluginClassSimpleName: String,
) {
    val pluginId = dependency.get().pluginId
    register(pluginId) {
        id = pluginId
        implementationClass = "io.github.ackeecz.apythia.plugin.$pluginClassSimpleName"
    }
}
