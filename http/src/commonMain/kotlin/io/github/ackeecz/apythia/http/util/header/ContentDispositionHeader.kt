package io.github.ackeecz.apythia.http.util.header

internal data class ContentDispositionHeader(
    val name: String,
    val filename: String? = null,
)
