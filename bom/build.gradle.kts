import io.github.ackeecz.apythia.verification.task.VerifyBomVersionTask
import org.gradle.api.internal.catalog.DelegatingProjectDependency

plugins {
    `java-platform`
    alias(libs.plugins.apythia.publishing)
}

dependencies {
    constraints {
        api(projects.http)
        api(projects.httpExtJsonKotlinxSerialization)
        api(projects.httpKtor)
        api(projects.httpOkhttp)
    }
}

private fun DependencyConstraintHandlerScope.api(dependency: DelegatingProjectDependency) {
    add("api", dependency)
}

VerifyBomVersionTask.registerFor(project)
