package io.github.ackeecz.apythia.http.request

import io.github.ackeecz.apythia.http.ExperimentalHttpApi
import io.github.ackeecz.apythia.http.util.header.contentType

/**
 * Subset of [ActualRequest] that groups together [headers] and [body].
 */
@ExperimentalHttpApi
public class ActualHttpMessage(
    public val headers: Map<String, List<String>>,
    public val body: ByteArray,
) {

    public val contentType: String? get() = headers.contentType
    public val contentLength: Int get() = body.size
}
