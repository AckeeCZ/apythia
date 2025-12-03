package io.github.ackeecz.apythia.http.request.dsl

import io.github.ackeecz.apythia.http.ExperimentalHttpApi
import io.github.ackeecz.apythia.http.extension.DslExtensionConfigProvider
import io.github.ackeecz.apythia.http.request.ActualRequest
import io.github.ackeecz.apythia.http.request.ExpectedRequest
import io.github.ackeecz.apythia.http.request.HttpMethod
import io.github.ackeecz.apythia.http.request.dsl.body.BodyAssertionImpl
import io.github.ackeecz.apythia.http.request.dsl.body.RequestBodyAssertion
import io.github.ackeecz.apythia.http.request.dsl.body.RequestBodyAssertionImpl
import io.github.ackeecz.apythia.http.request.dsl.header.HeadersAssertionImpl
import io.github.ackeecz.apythia.http.request.dsl.header.RequestHeadersAssertion
import io.github.ackeecz.apythia.http.request.dsl.header.RequestHeadersAssertionImpl
import io.github.ackeecz.apythia.http.request.dsl.url.UrlAssertion
import io.github.ackeecz.apythia.http.request.dsl.url.UrlAssertionImpl
import io.github.ackeecz.apythia.http.util.CallCountChecker

/**
 * Main entry point for HTTP request assertion DSL that allows to assert HTTP request properties.
 */
@HttpRequestDslMarker
@ExperimentalHttpApi
public interface HttpRequestAssertion {

    /**
     * Asserts HTTP method.
     */
    public fun method(method: HttpMethod)

    /**
     * Asserts HTTP request URL.
     */
    public fun url(assertUrl: UrlAssertion.() -> Unit)

    /**
     * Asserts HTTP request headers.
     */
    public fun headers(assertHeaders: RequestHeadersAssertion.() -> Unit)

    /**
     * Asserts HTTP request body.
     */
    public fun body(assertBody: RequestBodyAssertion.() -> Unit)
}

internal class HttpRequestAssertionImpl(
    private val configProvider: DslExtensionConfigProvider,
    private val actualRequest: ActualRequest,
) : HttpRequestAssertion {

    var expectedRequest = ExpectedRequest()
        private set

    private val methodCallCountChecker = CallCountChecker(actionName = "method")
    private val urlCallCountChecker = CallCountChecker(actionName = "url")
    private val headersCallCountChecker = CallCountChecker(actionName = "headers")
    private val bodyCallCountChecker = CallCountChecker(actionName = "body")

    override fun method(method: HttpMethod) {
        methodCallCountChecker.incrementOrFail()
        expectedRequest = expectedRequest.copy(method = method)
    }

    override fun url(assertUrl: UrlAssertion.() -> Unit) {
        urlCallCountChecker.incrementOrFail()
        val urlAssertion = UrlAssertionImpl().apply(assertUrl)
        expectedRequest = expectedRequest.copy(url = urlAssertion.expectedUrl)
    }

    override fun headers(assertHeaders: RequestHeadersAssertion.() -> Unit) {
        headersCallCountChecker.incrementOrFail()
        val headersAssertion = RequestHeadersAssertionImpl(HeadersAssertionImpl()).apply(assertHeaders)
        expectedRequest = expectedRequest.copy(headers = headersAssertion.expectedHeaders)
    }

    override fun body(assertBody: RequestBodyAssertion.() -> Unit) {
        bodyCallCountChecker.incrementOrFail()
        val bodyAssertion = BodyAssertionImpl(configProvider, actualRequest)
        val requestBodyAssertion = RequestBodyAssertionImpl(bodyAssertion).apply(assertBody)
        expectedRequest = expectedRequest.copy(body = requestBodyAssertion.expectedBody)
    }
}
