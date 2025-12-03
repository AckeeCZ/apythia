package io.github.ackeecz.apythia.http.request.dsl.header

import io.github.ackeecz.apythia.http.ExperimentalHttpApi
import io.github.ackeecz.apythia.http.request.dsl.HttpRequestDslMarker
import io.github.ackeecz.apythia.http.request.header.ExpectedHeaders
import io.github.ackeecz.apythia.http.util.CallCountChecker
import io.github.ackeecz.apythia.http.util.header.Headers
import io.github.ackeecz.apythia.http.util.header.appendHeaderParameters

/**
 * Provides various methods for HTTP headers assertions.
 */
@HttpRequestDslMarker
@ExperimentalHttpApi
public interface HeadersAssertion {

    /**
     * Asserts a [value] for a header with the given [name].
     */
    public fun header(name: String, value: String)

    /**
     * Asserts multiple [values] for a header with the given [name]. This can be either a comma-separated
     * list of values of a single header or multiple headers with the same name.
     *
     * @param name The name of the header.
     * @param values The values of the header. Values must not be empty.
     */
    public fun headers(name: String, values: List<String>)

    /**
     * Asserts a Content-Type header with the given [mimeType] and optional [parameters].
     * [parameters] has to be ordered the same way as they appear in the actual header.
     */
    public fun contentType(mimeType: String, parameters: Map<String, String> = emptyMap())
}

internal class HeadersAssertionImpl : HeadersAssertion {

    var expectedHeaders: ExpectedHeaders = ExpectedHeaders()
        private set

    private var headers: Map<String, List<String>>?
        get() = expectedHeaders.headers
        set(value) {
            expectedHeaders = expectedHeaders.copy(headers = value)
        }

    private val contentTypeCallCountChecker = CallCountChecker(actionName = "Content-Type")

    override fun header(name: String, value: String) {
        headers(name, listOf(value))
    }

    override fun headers(name: String, values: List<String>) {
        require(values.isNotEmpty()) { "values must not be empty" }
        if (headers == null) {
            headers = mutableMapOf()
        }
        headers = checkNotNull(headers).toMutableMap().also {
            it[name] = it.getOrElse(name) { emptyList() } + values
        }
    }

    override fun contentType(mimeType: String, parameters: Map<String, String>) {
        contentTypeCallCountChecker.incrementOrFail()
        header(Headers.CONTENT_TYPE, mimeType.appendHeaderParameters(parameters))
    }
}
