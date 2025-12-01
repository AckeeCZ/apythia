package io.github.ackeecz.apythia.util

internal sealed interface PublishableProject {

    val projectName: String

    object Bom : PublishableProject {

        override val projectName = "bom"
    }

    object Http : PublishableProject {

        override val projectName = "http"
    }
}
