import io.github.ackeecz.apythia.verification.task.VerifyBomVersionTask
import org.gradle.api.internal.catalog.DelegatingProjectDependency

plugins {
    `java-platform`
    alias(libs.plugins.ackeecz.apythia.publishing)
}

dependencies {
    constraints {
        api(projects.http)
    }
}

private fun DependencyConstraintHandlerScope.api(dependency: DelegatingProjectDependency) {
    add("api", dependency)
}

VerifyBomVersionTask.registerFor(project)
