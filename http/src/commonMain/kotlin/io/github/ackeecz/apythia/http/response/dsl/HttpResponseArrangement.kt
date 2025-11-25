package io.github.ackeecz.apythia.http.response.dsl

import io.github.ackeecz.apythia.http.Charset
import io.github.ackeecz.apythia.http.ExperimentalHttpApi
import io.github.ackeecz.apythia.http.HttpApythia
import io.github.ackeecz.apythia.http.extension.DslExtensionConfigProvider
import io.github.ackeecz.apythia.http.response.HttpResponse
import io.github.ackeecz.apythia.http.util.CallCountChecker
import io.github.ackeecz.apythia.http.util.header.Headers
import io.github.ackeecz.apythia.http.util.header.containsContentType

/**
 * Entry point for building a HTTP response.
 */
@HttpResponseDslMarker
public interface HttpResponseArrangement : DslExtensionConfigProvider {

    /**
     * Sets the HTTP status [code] for the response.
     *
     * @param code HTTP status code. Default is 200 if not set explicitly.
     */
    // We might want to limit status codes to valid ranges using type safety
    @ExperimentalHttpApi
    public fun statusCode(code: Int)

    /**
     * Sets the response headers using the provided [arrangeHeaders] block. Headers in the response
     * should be empty if not set explicitly, but it is possible that some [HttpApythia]
     * implementations set some headers automatically (e.g. Content-Length by OkHttpHttpApythia).
     */
    public fun headers(arrangeHeaders: HeadersArrangement.() -> Unit)

    /**
     * Sets the [value] as response body with a specified [contentType]. If [contentType] is null,
     * no content type header is set.
     */
    // We need to test the design of setting bodies like this and together with contentType header
    @ExperimentalHttpApi
    public fun bytesBody(value: ByteArray, contentType: String?)

    /**
     * Sets the [value] as plain text response body encoded with the specified [charset].
     *
     * Content type header is set to "text/plain" with the specified [charset] parameter. If you
     * want to omit [charset] parameter or set a custom content type, use [bytesBody] instead.
     * Default charset is UTF-8.
     */
    @ExperimentalHttpApi
    public fun plainTextBody(value: String, charset: Charset = Charset.UTF_8)
}

internal class HttpResponseArrangementImpl(
    private val dslExtensionConfigProvider: DslExtensionConfigProvider,
) : HttpResponseArrangement, DslExtensionConfigProvider by dslExtensionConfigProvider {

    private val statusCodeCallCountChecker = CallCountChecker("statusCode", maxCallCount = 1)
    private val headersCallCountChecker = CallCountChecker("headers", maxCallCount = 1)
    private val bodyCallCountChecker = CallCountChecker("body", maxCallCount = 1)

    var httpResponse = HttpResponse(
        statusCode = 200,
        headers = emptyMap(),
        body = byteArrayOf(),
    )
        private set

    override fun statusCode(code: Int) {
        statusCodeCallCountChecker.incrementOrFail()
        httpResponse = httpResponse.copy(statusCode = code)
    }

    override fun headers(arrangeHeaders: HeadersArrangement.() -> Unit) {
        headersCallCountChecker.incrementOrFail()
        val headers = HeadersArrangementImpl().apply(arrangeHeaders).headers
        if (headers.containsContentType) {
            checkContentTypeNotSet()
        }
        addHeaders(headers)
    }

    private fun checkContentTypeNotSet() {
        if (httpResponse.headers.containsContentType) {
            error("${Headers.CONTENT_TYPE} header is already present")
        }
    }

    private fun addHeaders(newHeaders: Map<String, List<String>>) {
        httpResponse = httpResponse.copy(
            headers = httpResponse.headers.plus(newHeaders)
        )
    }

    override fun bytesBody(value: ByteArray, contentType: String?) {
        bodyCallCountChecker.incrementOrFail()
        httpResponse = httpResponse.copy(body = value)
        if (contentType != null) {
            checkContentTypeNotSet()
            addHeaders(mapOf(Headers.CONTENT_TYPE to listOf(contentType)))
        }
    }

    override fun plainTextBody(value: String, charset: Charset) {
        val encodedValue = when (charset) {
            Charset.UTF_8 -> value.encodeToByteArray()
        }
        bytesBody(
            value = encodedValue,
            contentType = "text/plain; charset=${charset.name}",
        )
    }
}
