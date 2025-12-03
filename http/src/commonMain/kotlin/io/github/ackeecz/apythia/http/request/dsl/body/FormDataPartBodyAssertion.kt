package io.github.ackeecz.apythia.http.request.dsl.body

import io.github.ackeecz.apythia.http.ExperimentalHttpApi
import io.github.ackeecz.apythia.http.request.body.ExpectedFormDataPart
import io.github.ackeecz.apythia.http.request.dsl.HttpRequestDslMarker

/**
 * Provides various methods for multipart/form-data part body assertions.
 */
@HttpRequestDslMarker
@ExperimentalHttpApi
public interface FormDataPartBodyAssertion : BodyAssertion

internal class FormDataPartBodyAssertionImpl(
    private val bodyAssertion: BodyAssertionImpl,
) : FormDataPartBodyAssertion, BodyAssertion by bodyAssertion {

    val expectedBody: ExpectedFormDataPart.Body
        get() {
            return ExpectedFormDataPart.Body(
                body = bodyAssertion.expectedBody,
            )
        }
}
