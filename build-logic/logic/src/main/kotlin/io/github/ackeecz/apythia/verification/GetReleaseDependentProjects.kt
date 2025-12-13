package io.github.ackeecz.apythia.verification

import io.github.ackeecz.apythia.util.ApythiaPublishableProject
import io.github.ackeecz.apythia.util.PublishableProject
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectSet
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.provider.Provider
import org.jetbrains.annotations.VisibleForTesting

/**
 * Returns list of projects that depend on passed [Project] but only using release configurations
 * like "implementation", "api", "releaseImplementation", etc. that are relevant for publishing.
 */
internal interface GetReleaseDependentProjects {

    operator fun invoke(project: Project): Provider<List<Project>>

    companion object {

        operator fun invoke(): GetReleaseDependentProjects {
            return GetReleaseDependentProjectsImpl(ApythiaPublishableProject.entries)
        }
    }
}

@VisibleForTesting
internal class GetReleaseDependentProjectsImpl(
    private val publishableProjects: List<PublishableProject>,
) : GetReleaseDependentProjects {

    override fun invoke(project: Project): Provider<List<Project>> {
        val publishableProjectNames = publishableProjects.map { it.projectName }
        val dependentProjects = mutableListOf<Project>()
        project.rootProject
            .getWholeHierarchyChildProjects()
            .filterNot { it.path == project.path } // Filter out current project from check
            .forEach { checkedProject ->
                checkedProject.configurations
                    .filterAcceptableConfigurations()
                    .all(Action {
                        val dependencyOnTargetProject = dependencies
                            .filterIsInstance<ProjectDependency>()
                            .distinctBy { it.path }
                            .find { it.path == project.path }
                        if (dependencyOnTargetProject != null && checkedProject.name in publishableProjectNames) {
                            dependentProjects.add(checkedProject)
                        }
                    })
            }
        return project.provider { dependentProjects }
    }

    private fun Project.getWholeHierarchyChildProjects(): List<Project> {
        return childProjects.values + childProjects.values.flatMap { it.getWholeHierarchyChildProjects() }
    }

    private fun ConfigurationContainer.filterAcceptableConfigurations(): NamedDomainObjectSet<Configuration> {
        return matching {
            ACCEPTABLE_CONFIGURATIONS.contains(it.name) || it.name.startsWith(RELEASE_CONFIGURATION_PREFIX)
        }
    }

    companion object {

        private val ACCEPTABLE_CONFIGURATIONS = listOf(
            // Android/JVM projects
            "api",
            "compileOnly",
            "compileOnlyApi",
            "implementation",
            "runtimeOnly",
            // KMP projects
            "commonMainApi",
            "commonMainCompileOnly",
            "commonMainImplementation",
            "commonMainRuntimeOnly",
        )

        private const val RELEASE_CONFIGURATION_PREFIX = "release"
    }
}
