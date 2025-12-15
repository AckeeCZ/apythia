package io.github.ackeecz.apythia.verification

import io.github.ackeecz.apythia.testutil.IMPLEMENTATION_CONFIGURATION
import io.github.ackeecz.apythia.testutil.addDependencies
import io.github.ackeecz.apythia.testutil.addImplementationDependencies
import io.github.ackeecz.apythia.testutil.buildProject
import io.github.ackeecz.apythia.util.PublishableProject
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import org.gradle.api.Project

private lateinit var underTest: GetReleaseDependentProjects

internal class GetReleaseDependentProjectsTest : FunSpec({

    beforeTest {
        underTest = GetReleaseDependentProjectsImpl(TestPublishableProject.entries)
    }

    test("get all dependent projects for complex flat hierarchy (root and child projects)") {
        // Arrange
        val rootProject = buildProject(name = "root")
        val checkedProject = buildProject(name = TestPublishableProject.One.projectName, parent = rootProject)
        val notDependentProject1 = buildProject(name = TestPublishableProject.Two.projectName, parent = rootProject)
        val notDependentProject2 = buildProject(name = TestPublishableProject.Three.projectName, parent = rootProject)
            .addImplementationDependencies(notDependentProject1)

        val dependentProject1 = buildProject(name = TestPublishableProject.Four.projectName, parent = rootProject)
            .addImplementationDependencies(checkedProject, notDependentProject2)

        val dependentProject2 = buildProject(name = TestPublishableProject.Five.projectName, parent = rootProject)
            .addImplementationDependencies(checkedProject, dependentProject1)

        // Act
        val actual = underTest(checkedProject).get()

        // Assert
        actual shouldContainProjectsExactlyInAnyOrder listOf(dependentProject1, dependentProject2)
    }

    context("get dependent projects for release configurations") {
        withData(
            "api",
            "compileOnly",
            "compileOnlyApi",
            IMPLEMENTATION_CONFIGURATION,
            "releaseApi",
            "releaseCompileOnly",
            "releaseCompileOnlyApi",
            "releaseImplementation",
            "releaseRuntimeOnly",
            "runtimeOnly",
            "commonMainApi",
            "commonMainCompileOnly",
            "commonMainImplementation",
            "commonMainRuntimeOnly",
        ) { configuration ->
            val rootProject = buildProject(name = "root")
            val checkedProject = buildProject(name = TestPublishableProject.One.projectName, parent = rootProject)
            val notDependentProject = buildProject(name = TestPublishableProject.Two.projectName, parent = rootProject)
            val dependentProject = buildProject(name = TestPublishableProject.Three.projectName, parent = rootProject)
                .addDependencies(configuration, checkedProject, notDependentProject)

            val actual = underTest(checkedProject).get()

            actual shouldContainProjectsExactlyInAnyOrder listOf(dependentProject)
        }
    }

    context("get no dependent projects for non-release configurations") {
        withData(
            "androidTestApi",
            "androidTestImplementation",
            "debugApi",
            "debugImplementation",
            "testDebugImplementation",
            "testFixturesImplementation",
            "testImplementation",
        ) { configuration ->
            val rootProject = buildProject(name = "root")
            val checkedProject = buildProject(name = TestPublishableProject.One.projectName, parent = rootProject)
            buildProject(name = TestPublishableProject.Two.projectName, parent = rootProject)
                .addDependencies(configuration, checkedProject)

            val actual = underTest(checkedProject).get()

            actual.shouldBeEmpty()
        }
    }

    test("get all dependent projects for complex nested hierarchy (root, child, grandchild projects)") {
        // Arrange
        val rootProject = buildProject(name = "root")
        val checkedProject = buildProject(name = TestPublishableProject.One.projectName, parent = rootProject)
        val notDependentChildProject = buildProject(name = TestPublishableProject.Two.projectName, parent = rootProject)
        val dependentChildProject = buildProject(name = TestPublishableProject.Three.projectName, parent = rootProject)
            .addImplementationDependencies(checkedProject, notDependentChildProject)

        val notDependentGrandChildProject = buildProject(
            name = TestPublishableProject.Four.projectName,
            parent = notDependentChildProject,
        )
        val dependentGrandChildProject = buildProject(
            name = TestPublishableProject.Five.projectName,
            parent = dependentChildProject,
        ).addImplementationDependencies(checkedProject, notDependentGrandChildProject)

        // Act
        val actual = underTest(checkedProject).get()

        // Assert
        actual shouldContainProjectsExactlyInAnyOrder listOf(dependentChildProject, dependentGrandChildProject)
    }

    // This is happening for some unit test configurations that they depend on itself, so we need to
    // filter these cases out, because we care only about other dependent projects
    test("get no dependent projects when project depend only on itself") {
        val rootProject = buildProject(name = "root")
        val checkedProject = buildProject(name = TestPublishableProject.One.projectName, parent = rootProject).also {
            it.addDependencies("releaseUnitTestCompileClasspath", it)
        }

        val actual = underTest(checkedProject).get()

        actual.shouldBeEmpty()
    }

    test("filter out dependent projects that are not publishable") {
        val rootProject = buildProject(name = "root")
        val checkedProject = buildProject(name = TestPublishableProject.One.projectName, parent = rootProject)
        buildProject(name = "not-publishable", parent = rootProject)
            .addImplementationDependencies(checkedProject)

        val actual = underTest(checkedProject).get()

        actual.shouldBeEmpty()
    }
})

private infix fun List<Project>.shouldContainProjectsExactlyInAnyOrder(expected: List<Project>) {
    map { it.path } shouldContainExactlyInAnyOrder expected.map { it.path }
}

private enum class TestPublishableProject(override val projectName: String) : PublishableProject {

    One("one"),
    Two("two"),
    Three("three"),
    Four("four"),
    Five("five"),
}
