package io.github.ackeecz.apythia.http.request.body

import io.github.ackeecz.apythia.http.util.header.appendHeaderParameters

internal fun createActualPart(
    headers: Map<String, List<String>> = emptyMap(),
    body: ByteArray = byteArrayOf(),
): ActualPart {
    return ActualPart(
        headers = headers,
        body = body,
    )
}

internal fun createActualFormDataPart(
    name: String = "name",
    filename: String? = null,
    headers: Map<String, List<String>> = emptyMap(),
    body: ByteArray = byteArrayOf(),
): ActualPart {
    val parameters = mutableMapOf<String, String>()
    parameters["name"] = name
    if (filename != null) {
        parameters["filename"] = filename
    }
    val contentDispositionValue = "form-data".appendHeaderParameters(parameters)
    val contentDispositionHeader = mapOf("Content-Disposition" to listOf(contentDispositionValue))
    return createActualPart(
        headers = headers + contentDispositionHeader,
        body = body,
    )
}
