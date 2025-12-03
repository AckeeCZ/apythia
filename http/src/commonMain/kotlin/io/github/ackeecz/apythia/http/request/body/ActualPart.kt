package io.github.ackeecz.apythia.http.request.body

import io.github.ackeecz.apythia.http.ExperimentalHttpApi

/**
 * Actual multipart body part that was sent by the HTTP client. This data is used for assertions.
 */
@ExperimentalHttpApi
public class ActualPart(
    internal val headers: Map<String, List<String>>,
    internal val body: ByteArray
)
