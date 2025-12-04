package io.github.ackeecz.apythia.http.request.dsl.body

import io.github.ackeecz.apythia.http.ExperimentalHttpApi
import io.github.ackeecz.apythia.http.extension.DslExtensionConfigProvider
import io.github.ackeecz.apythia.http.request.ActualHttpMessage
import io.github.ackeecz.apythia.http.request.body.ActualFormDataPart
import io.github.ackeecz.apythia.http.request.body.ActualPart
import io.github.ackeecz.apythia.http.request.dsl.HttpRequestDslMarker
import io.github.ackeecz.apythia.http.request.dsl.header.FormDataPartHeadersAssertion
import io.github.ackeecz.apythia.http.request.dsl.header.FormDataPartHeadersAssertionImpl
import io.github.ackeecz.apythia.http.request.dsl.header.HeadersAssertionImpl
import io.github.ackeecz.apythia.http.util.CallCountChecker

/**
 * Provides various methods for multipart/form-data part assertions.
 */
@HttpRequestDslMarker
@ExperimentalHttpApi
public interface FormDataPartAssertion {

    /**
     * Asserts HTTP headers of the multipart/form-data part.
     */
    public fun headers(assertHeaders: FormDataPartHeadersAssertion.() -> Unit)

    /**
     * Asserts HTTP body of the multipart/form-data part.
     */
    public suspend fun body(assertBody: suspend FormDataPartBodyAssertion.() -> Unit)
}

internal class FormDataPartAssertionImpl(
    private val configProvider: DslExtensionConfigProvider,
    private val actualPart: ActualFormDataPart,
    private val collectNestedParts: suspend (ActualHttpMessage) -> List<ActualPart>,
) : FormDataPartAssertion {

    private val headersCallCountChecker = CallCountChecker(actionName = "headers")
    private val bodyCallCountChecker = CallCountChecker(actionName = "body")

    override fun headers(assertHeaders: FormDataPartHeadersAssertion.() -> Unit) {
        headersCallCountChecker.incrementOrFail()
        val headersAssertion = HeadersAssertionImpl(
            configProvider = configProvider,
            actualHeaders = actualPart.message.headers,
        )
        FormDataPartHeadersAssertionImpl(headersAssertion).assertHeaders()
    }

    override suspend fun body(assertBody: suspend FormDataPartBodyAssertion.() -> Unit) {
        bodyCallCountChecker.incrementOrFail()
        val bodyAssertion = BodyAssertionImpl(
            configProvider = configProvider,
            actualMessage = actualPart.message,
            collectMultipartParts = collectNestedParts,
        )
        FormDataPartBodyAssertionImpl(bodyAssertion).assertBody()
    }
}
