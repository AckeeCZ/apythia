package io.github.ackeecz.apythia.http.request.dsl.headers

import io.github.ackeecz.apythia.http.ExperimentalHttpApi
import io.github.ackeecz.apythia.http.request.ExpectedRequest
import io.github.ackeecz.apythia.http.request.dsl.HttpRequestDsl

/**
 * Provides various methods for HTTP request headers assertions.
 */
@HttpRequestDsl
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
