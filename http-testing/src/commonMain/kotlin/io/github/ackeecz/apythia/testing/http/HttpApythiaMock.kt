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

    public var actualResponse: HttpResponse? = null
        private set

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
        actualResponse = response
    }

    override suspend fun getNextActualRequest(): ActualRequest = actualRequest

    override suspend fun forEachMultipartFormDataPart(
        message: ActualHttpMessage,
        onPart: suspend (ActualPart) -> Unit,
    ) {
        actualParts.forEach { onPart(it) }
    }
}
