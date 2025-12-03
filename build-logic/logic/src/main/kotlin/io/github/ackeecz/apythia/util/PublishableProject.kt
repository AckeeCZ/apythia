package io.github.ackeecz.apythia.util

internal sealed interface PublishableProject {

    val projectName: String

    object Bom : PublishableProject {

        override val projectName = "bom"
    }

    object Http : PublishableProject {

        override val projectName = "http"
    }

    object HttpExtJsonKotlinxSerialization : PublishableProject {

        override val projectName = "http-ext-json-kotlinx-serialization"
    }

    object HttpKtor : PublishableProject {
        override val projectName = "http-ktor"
    }


    object HttpOkhttp : PublishableProject {
        override val projectName = "http-okhttp"
    }
}
