package io.github.ackeecz.apythia.testing.http.request

import io.github.ackeecz.apythia.http.request.ActualRequest

public fun createActualRequest(
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
