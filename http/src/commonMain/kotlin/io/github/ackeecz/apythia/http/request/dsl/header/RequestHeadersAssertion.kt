package io.github.ackeecz.apythia.http.request.dsl.header

import io.github.ackeecz.apythia.http.ExperimentalHttpApi
import io.github.ackeecz.apythia.http.request.ExpectedRequest
import io.github.ackeecz.apythia.http.request.dsl.HttpRequestDslMarker

/**
 * Provides various methods for HTTP request headers assertions.
 */
@HttpRequestDslMarker
@ExperimentalHttpApi
public interface RequestHeadersAssertion : HeadersAssertion

internal class RequestHeadersAssertionImpl(
    private val headersAssertion: HeadersAssertionImpl,
) : RequestHeadersAssertion, HeadersAssertion by headersAssertion {

    val expectedHeaders: ExpectedRequest.Headers
        get() {
            return ExpectedRequest.Headers(
                headers = headersAssertion.expectedHeaders,
            )
        }
}
