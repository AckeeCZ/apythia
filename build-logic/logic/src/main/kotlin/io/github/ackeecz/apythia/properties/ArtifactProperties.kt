package io.github.ackeecz.apythia.properties

import io.github.ackeecz.apythia.util.PublishableProject
import java.util.Properties

public sealed class ArtifactProperties(
    private val properties: Properties,
    private val defaultPropertyPrefix: String,
    versionPropertyPrefix: String = defaultPropertyPrefix,
) {

    public val id: String = getPropertyWithDefaultPrefix("ARTIFACT_ID")
    public val version: String = getProperty(prefix = versionPropertyPrefix, name = "VERSION")
    public val pomName: String = getPropertyWithDefaultPrefix("POM_NAME")
    public val pomYear: String = getPropertyWithDefaultPrefix("POM_YEAR")
    public val pomDescription: String = getPropertyWithDefaultPrefix("POM_DESCRIPTION")

    private fun getPropertyWithDefaultPrefix(name: String): String {
        return getProperty(prefix = defaultPropertyPrefix, name = name)
    }

    private fun getProperty(prefix: String, name: String): String {
        return properties.getNonNull("${prefix}_$name")
    }

    public class Bom(properties: Properties) : ArtifactProperties(
        properties = properties,
        defaultPropertyPrefix = "BOM",
    )

    public class Http(properties: Properties) : ArtifactProperties(
        properties = properties,
        defaultPropertyPrefix = "HTTP",
    )

    public class HttpExtJsonKotlinxSerialization(properties: Properties) : ArtifactProperties(
        properties = properties,
        defaultPropertyPrefix = "HTTP_EXT_JSON_KOTLINX_SERIALIZATION",
    )

    public class HttpKtor(properties: Properties) : ArtifactProperties(
        properties = properties,
        defaultPropertyPrefix = "HTTP_KTOR",
    )

    public class HttpOkhttp(properties: Properties) : ArtifactProperties(
        properties = properties,
        defaultPropertyPrefix = "HTTP_OKHTTP",
    )

    internal companion object {

        fun getFor(
            projectName: String,
            properties: Properties,
        ): ArtifactProperties = when (projectName) {
            PublishableProject.Bom.projectName -> Bom(properties)
            PublishableProject.Http.projectName -> Http(properties)
            PublishableProject.HttpExtJsonKotlinxSerialization.projectName -> HttpExtJsonKotlinxSerialization(properties)
            PublishableProject.HttpKtor.projectName -> HttpKtor(properties)
            PublishableProject.HttpOkhttp.projectName -> HttpOkhttp(properties)
            else -> throw IllegalStateException("Unknown Gradle module with name $projectName. Please " +
                "add artifact properties for this module and corresponding mapping in " +
                "${ArtifactProperties::class.simpleName}. It is also possible that you changed module " +
                "name and in that case update the mapping as well.")
        }
    }
}
