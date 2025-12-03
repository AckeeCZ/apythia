package io.github.ackeecz.apythia.http.request

import io.github.ackeecz.apythia.http.ExperimentalHttpApi
import io.github.ackeecz.apythia.http.util.header.contentType

/**
 * Subset of [ActualRequest] that groups together [headers] and [body].
 */
@ExperimentalHttpApi
public class ActualHttpMessage(
    internal val headers: Map<String, List<String>>,
    internal val body: ByteArray,
) {

    internal val contentType get() = headers.contentType
    internal val contentLength: Int get() = body.size
}
