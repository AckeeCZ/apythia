package io.github.ackeecz.apythia.http.request.dsl.body

/**
 * The actual body of a HTTP request. This can be used to extend the [BodyAssertion] DSL with
 * custom assertions.
 *
 * @param data The raw bytes of the body.
 * @param contentType The content type of the body, if available. It matches the Content-Type header
 * value.
 */
public class ActualBody(
    public val data: ByteArray,
    public val contentType: String?,
)
