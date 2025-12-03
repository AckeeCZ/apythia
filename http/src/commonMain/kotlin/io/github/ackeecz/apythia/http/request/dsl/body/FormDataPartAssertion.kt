package io.github.ackeecz.apythia.http.request.dsl.body

import io.github.ackeecz.apythia.http.ExperimentalHttpApi
import io.github.ackeecz.apythia.http.extension.DslExtensionConfigProvider
import io.github.ackeecz.apythia.http.request.ActualRequest
import io.github.ackeecz.apythia.http.request.body.ExpectedFormDataPart
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
    public fun body(assertBody: FormDataPartBodyAssertion.() -> Unit)
}

internal class FormDataPartAssertionImpl(
    private val configProvider: DslExtensionConfigProvider,
    private val actualRequest: ActualRequest,
) : FormDataPartAssertion {

    var expectedHeaders: ExpectedFormDataPart.Headers = ExpectedFormDataPart.Headers()
        private set

    var expectedBody: ExpectedFormDataPart.Body = ExpectedFormDataPart.Body()
        private set

    private val headersCallCountChecker = CallCountChecker(actionName = "headers")
    private val bodyCallCountChecker = CallCountChecker(actionName = "body")

    override fun headers(assertHeaders: FormDataPartHeadersAssertion.() -> Unit) {
        headersCallCountChecker.incrementOrFail()
        val headersAssertion = FormDataPartHeadersAssertionImpl(HeadersAssertionImpl()).apply(assertHeaders)
        expectedHeaders = headersAssertion.expectedHeaders
    }

    override fun body(assertBody: FormDataPartBodyAssertion.() -> Unit) {
        bodyCallCountChecker.incrementOrFail()
        val bodyAssertion = BodyAssertionImpl(configProvider, actualRequest)
        val partBodyAssertion = FormDataPartBodyAssertionImpl(bodyAssertion).apply(assertBody)
        expectedBody = partBodyAssertion.expectedBody
    }
}
