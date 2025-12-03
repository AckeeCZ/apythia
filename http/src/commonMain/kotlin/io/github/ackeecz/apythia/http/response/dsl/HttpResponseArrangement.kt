package io.github.ackeecz.apythia.http.response.dsl

import io.github.ackeecz.apythia.http.Charset
import io.github.ackeecz.apythia.http.ExperimentalHttpApi
import io.github.ackeecz.apythia.http.HttpApythia
import io.github.ackeecz.apythia.http.response.HttpResponse
import io.github.ackeecz.apythia.http.util.CallCountChecker
import io.github.ackeecz.apythia.http.util.Headers
import io.github.ackeecz.apythia.http.util.containsContentType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArrayBuilder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject

/**
 * Entry point for building a HTTP response.
 */
@HttpResponseDsl
public interface HttpResponseArrangement {

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

    /**
     * Sets the [value] as JSON response body.
     *
     * Content type header is set to "application/json" by default. If you want to omit this header
     * set [includeContentTypeHeader] to false.
     */
    @ExperimentalHttpApi
    public fun jsonBody(value: JsonElement, includeContentTypeHeader: Boolean = true)

    /**
     * Same as [jsonBody] accepting [JsonElement] but takes a JSON string as input.
     */
    @ExperimentalHttpApi
    public fun jsonBody(value: String, includeContentTypeHeader: Boolean = true)

    /**
     * Same as [jsonBody] but uses a builder block to create a JSON object.
     */
    @ExperimentalHttpApi
    public fun jsonObjectBody(
        includeContentTypeHeader: Boolean = true,
        build: JsonObjectBuilder.() -> Unit,
    )

    /**
     * Same as [jsonBody] but uses a builder block to create a JSON array.
     */
    @ExperimentalHttpApi
    public fun jsonArrayBody(
        includeContentTypeHeader: Boolean = true,
        build: JsonArrayBuilder.() -> Unit,
    )
}

internal class HttpResponseArrangementImpl(
    private val json: Json,
) : HttpResponseArrangement {

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

    override fun jsonBody(value: JsonElement, includeContentTypeHeader: Boolean) {
        jsonBody(
            value = json.encodeToString(value),
            includeContentTypeHeader = includeContentTypeHeader,
        )
    }

    override fun jsonBody(value: String, includeContentTypeHeader: Boolean) {
        bytesBody(
            value = value.encodeToByteArray(),
            contentType = if (includeContentTypeHeader) "application/json" else null
        )
    }

    override fun jsonObjectBody(
        includeContentTypeHeader: Boolean,
        build: JsonObjectBuilder.() -> Unit,
    ) {
        jsonBody(
            value = buildJsonObject(build),
            includeContentTypeHeader = includeContentTypeHeader,
        )
    }

    override fun jsonArrayBody(
        includeContentTypeHeader: Boolean,
        build: JsonArrayBuilder.() -> Unit,
    ) {
        jsonBody(
            value = buildJsonArray(build),
            includeContentTypeHeader = includeContentTypeHeader,
        )
    }
}
