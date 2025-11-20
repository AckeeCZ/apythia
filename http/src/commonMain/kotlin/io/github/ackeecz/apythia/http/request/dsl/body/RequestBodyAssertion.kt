package io.github.ackeecz.apythia.http.request.dsl.body

import io.github.ackeecz.apythia.http.ExperimentalHttpApi
import io.github.ackeecz.apythia.http.request.ExpectedRequest
import io.github.ackeecz.apythia.http.request.dsl.HttpRequestDsl

/**
 * Provides various methods for HTTP request body assertions.
 */
@HttpRequestDsl
@ExperimentalHttpApi
public interface RequestBodyAssertion : BodyAssertion

internal class RequestBodyAssertionImpl(
    private val bodyAssertion: BodyAssertionImpl,
) : RequestBodyAssertion, BodyAssertion by bodyAssertion {

    val expectedBody: ExpectedRequest.Body
        get() {
            return ExpectedRequest.Body(
                body = bodyAssertion.expectedBody,
            )
        }
}
