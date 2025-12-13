package io.github.ackeecz.apythia.util

internal interface PublishableProject {

    val projectName: String
}

internal enum class ApythiaPublishableProject(override val projectName: String) : PublishableProject {

    Bom("bom"),
    Http("http"),
    HttpExtJsonKotlinxSerialization("http-ext-json-kotlinx-serialization"),
    HttpKtor("http-ktor"),
    HttpOkhttp("http-okhttp"),
}
