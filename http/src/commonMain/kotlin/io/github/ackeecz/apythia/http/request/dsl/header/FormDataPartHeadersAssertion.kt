package io.github.ackeecz.apythia.http.request.dsl.header

import io.github.ackeecz.apythia.http.ExperimentalHttpApi
import io.github.ackeecz.apythia.http.request.body.ExpectedFormDataPart
import io.github.ackeecz.apythia.http.request.dsl.HttpRequestDslMarker

/**
 * Provides various methods for multipart/form-data part headers assertions.
 */
@HttpRequestDslMarker
@ExperimentalHttpApi
public interface FormDataPartHeadersAssertion : HeadersAssertion

internal class FormDataPartHeadersAssertionImpl(
    private val headersAssertion: HeadersAssertionImpl,
) : FormDataPartHeadersAssertion, HeadersAssertion by headersAssertion {

    val expectedHeaders: ExpectedFormDataPart.Headers
        get() {
            return ExpectedFormDataPart.Headers(
                headers = headersAssertion.expectedHeaders,
            )
        }
}
