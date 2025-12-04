package io.github.ackeecz.apythia.http.request.dsl

import io.github.ackeecz.apythia.http.ExperimentalHttpApi
import io.github.ackeecz.apythia.http.extension.DslExtensionConfigProvider
import io.github.ackeecz.apythia.http.request.ActualHttpMessage
import io.github.ackeecz.apythia.http.request.ActualRequest
import io.github.ackeecz.apythia.http.request.HttpMethod
import io.github.ackeecz.apythia.http.request.body.ActualPart
import io.github.ackeecz.apythia.http.request.dsl.body.BodyAssertionImpl
import io.github.ackeecz.apythia.http.request.dsl.body.RequestBodyAssertion
import io.github.ackeecz.apythia.http.request.dsl.body.RequestBodyAssertionImpl
import io.github.ackeecz.apythia.http.request.dsl.header.HeadersAssertionImpl
import io.github.ackeecz.apythia.http.request.dsl.header.RequestHeadersAssertion
import io.github.ackeecz.apythia.http.request.dsl.header.RequestHeadersAssertionImpl
import io.github.ackeecz.apythia.http.request.dsl.url.UrlAssertion
import io.github.ackeecz.apythia.http.request.dsl.url.UrlAssertionImpl
import io.github.ackeecz.apythia.http.util.CallCountChecker
import io.kotest.matchers.shouldBe

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
    public suspend fun body(assertBody: suspend RequestBodyAssertion.() -> Unit)
}

internal class HttpRequestAssertionImpl(
    private val configProvider: DslExtensionConfigProvider,
    private val actualRequest: ActualRequest,
    private val collectMultipartParts: suspend (ActualHttpMessage) -> List<ActualPart>,
) : HttpRequestAssertion {

    private val urlCallCountChecker = CallCountChecker(actionName = "url")
    private val headersCallCountChecker = CallCountChecker(actionName = "headers")
    private val bodyCallCountChecker = CallCountChecker(actionName = "body")

    override fun method(method: HttpMethod) {
        actualRequest.method.lowercase() shouldBe method.value.lowercase()
    }

    override fun url(assertUrl: UrlAssertion.() -> Unit) {
        urlCallCountChecker.incrementOrFail()
        UrlAssertionImpl(
            configProvider = configProvider,
            actualTypedUrl = actualRequest.url,
        ).assertUrl()
    }

    override fun headers(assertHeaders: RequestHeadersAssertion.() -> Unit) {
        headersCallCountChecker.incrementOrFail()
        val headersAssertion = HeadersAssertionImpl(
            configProvider = configProvider,
            actualHeaders = actualRequest.headers,
        )
        RequestHeadersAssertionImpl(headersAssertion).assertHeaders()
    }

    override suspend fun body(assertBody: suspend RequestBodyAssertion.() -> Unit) {
        bodyCallCountChecker.incrementOrFail()
        val bodyAssertion = BodyAssertionImpl(
            configProvider = configProvider,
            actualMessage = actualRequest.message,
            collectMultipartParts = collectMultipartParts,
        )
        RequestBodyAssertionImpl(bodyAssertion).assertBody()
    }
}
