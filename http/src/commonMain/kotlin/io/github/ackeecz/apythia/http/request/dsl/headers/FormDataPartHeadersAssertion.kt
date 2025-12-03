package io.github.ackeecz.apythia.http.request.dsl.headers

import io.github.ackeecz.apythia.http.ExperimentalHttpApi
import io.github.ackeecz.apythia.http.request.body.ExpectedFormDataPart
import io.github.ackeecz.apythia.http.request.dsl.HttpRequestDsl

/**
 * Provides various methods for multipart/form-data part headers assertions.
 */
@HttpRequestDsl
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
