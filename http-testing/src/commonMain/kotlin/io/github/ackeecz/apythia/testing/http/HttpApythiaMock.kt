package io.github.ackeecz.apythia.testing.http

import io.github.ackeecz.apythia.http.HttpApythia
import io.github.ackeecz.apythia.http.extension.DslExtensionConfigs
import io.github.ackeecz.apythia.http.request.ActualHttpMessage
import io.github.ackeecz.apythia.http.request.ActualRequest
import io.github.ackeecz.apythia.http.request.body.ActualPart
import io.github.ackeecz.apythia.http.response.HttpResponse
import io.github.ackeecz.apythia.testing.http.request.createActualRequest

/**
 * [HttpApythia] mock implementation for testing purposes. This allows to test abstract [HttpApythia]
 * without using a real implementation.
 */
public class HttpApythiaMock(
    dslExtensionConfigs: DslExtensionConfigs.() -> Unit = {},
) : HttpApythia(
    dslExtensionConfigs = dslExtensionConfigs,
) {

    public var actualRequest: ActualRequest = createActualRequest()

    public var actualRequestMethod: String
        get() = actualRequest.method
        set(value) {
            actualRequest = actualRequest.copy(method = value)
        }

    public var actualRequestUrl: String
        get() = actualRequest.urlString
        set(value) {
            actualRequest = actualRequest.copy(url = value)
        }

    public var actualRequestHeaders: Map<String, List<String>>
        get() = actualRequest.headers
        set(value) {
            actualRequest = actualRequest.copy(headers = value)
        }

    public var actualRequestBody: ByteArray
        get() = actualRequest.body
        set(value) {
            actualRequest = actualRequest.copy(body = value)
        }

    public var actualParts: List<ActualPart> = emptyList()

    private val _mockedResponseProviders = mutableListOf<(ActualRequest) -> HttpResponse>()

    /**
     * Every response provider recorded through the mocking methods, in the mocking order.
     * Statically mocked responses are recorded as constant providers.
     */
    public val mockedResponseProviders: List<(ActualRequest) -> HttpResponse>
        get() = _mockedResponseProviders

    /**
     * Response of the last recorded provider from [mockedResponseProviders] resolved against
     * [actualRequest]. `null` if nothing was mocked yet.
     */
    public val actualResponse: HttpResponse?
        get() = mockedResponseProviders.lastOrNull()?.invoke(actualRequest)

    private fun ActualRequest.copy(
        method: String = this.method,
        url: String = this.urlString,
        headers: Map<String, List<String>> = this.headers,
        body: ByteArray = this.body,
    ): ActualRequest {
        return ActualRequest(
            method = method,
            url = url,
            headers = headers,
            body = body,
        )
    }

    override fun beforeEachTest(): Unit = Unit

    override fun afterEachTest(): Unit = Unit

    override fun mockNextResponse(response: HttpResponse) {
        _mockedResponseProviders.add { response }
    }

    override fun mockNextResponseProvider(provideResponse: (ActualRequest) -> HttpResponse) {
        _mockedResponseProviders.add(provideResponse)
    }

    override suspend fun getNextActualRequest(): ActualRequest = actualRequest

    override suspend fun forEachMultipartFormDataPart(
        message: ActualHttpMessage,
        onPart: suspend (ActualPart) -> Unit,
    ) {
        actualParts.forEach { onPart(it) }
    }
}
