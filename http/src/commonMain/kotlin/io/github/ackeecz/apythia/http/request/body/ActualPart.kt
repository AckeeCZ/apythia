package io.github.ackeecz.apythia.http.request.body

import io.github.ackeecz.apythia.http.ExperimentalHttpApi

/**
 * Actual multipart body part that was sent by the HTTP client. This data is used for assertions.
 */
@ExperimentalHttpApi
public class ActualPart(
    internal val headers: Map<String, List<String>>,
    internal val body: ByteArray
) {

    internal fun copy(
        headers: Map<String, List<String>> = this.headers,
        body: ByteArray = this.body,
    ): ActualPart {
        return ActualPart(headers = headers, body = body)
    }

    override fun toString(): String {
        return "ActualPart(headers=$headers, body=${body.toHexString()})"
    }
}
