package io.github.ackeecz.apythia.verification

import org.gradle.api.Project

internal class GetReleaseDependentProjectsStub : GetReleaseDependentProjects {

    var dependentProjects: List<Project> = emptyList()

    override fun invoke(project: Project): List<Project> = dependentProjects
}
