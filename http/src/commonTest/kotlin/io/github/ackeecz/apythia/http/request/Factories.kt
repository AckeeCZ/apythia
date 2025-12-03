package io.github.ackeecz.apythia.http.request

internal fun createActualRequest(
    method: String = "GET",
    url: String = "http://example.com",
    headers: Map<String, List<String>> = emptyMap(),
    body: ByteArray = byteArrayOf(),
): ActualRequest {
    return ActualRequest(
        method = method,
        url = url,
        headers = headers,
        body = body,
    )
}
