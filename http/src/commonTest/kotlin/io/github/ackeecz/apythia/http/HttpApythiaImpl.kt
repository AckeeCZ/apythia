package io.github.ackeecz.apythia.http

import io.github.ackeecz.apythia.http.extension.DslExtensionConfigs
import io.github.ackeecz.apythia.http.request.ActualHttpMessage
import io.github.ackeecz.apythia.http.request.ActualRequest
import io.github.ackeecz.apythia.http.request.body.ActualPart
import io.github.ackeecz.apythia.http.request.createActualRequest
import io.github.ackeecz.apythia.http.response.HttpResponse

/**
 * [HttpApythia] implementation for testing purposes. This allows to test abstract [HttpApythia]
 * without using a real implementation.
 */
internal class HttpApythiaImpl(
    dslExtensionConfigs: DslExtensionConfigs.() -> Unit = {},
) : HttpApythia(
    dslExtensionConfigs = dslExtensionConfigs,
) {

    var actualRequest: ActualRequest = createActualRequest()

    var actualMethod: String
        get() = actualRequest.method
        set(value) {
            actualRequest = actualRequest.copy(method = value)
        }

    var actualUrl: String
        get() = actualRequest.url.toString()
        set(value) {
            actualRequest = actualRequest.copy(url = value)
        }

    var actualHeaders: Map<String, List<String>>
        get() = actualRequest.message.headers
        set(value) {
            actualRequest = actualRequest.copy(headers = value)
        }

    var actualBody: ByteArray
        get() = actualRequest.message.body
        set(value) {
            actualRequest = actualRequest.copy(body = value)
        }

    var actualParts: List<ActualPart> = emptyList()

    override fun beforeEachTest() = Unit

    override fun afterEachTest() = Unit

    override fun arrangeNextResponse(response: HttpResponse) = Unit

    override suspend fun getNextActualRequest(): ActualRequest = actualRequest

    override suspend fun forEachMultipartFormDataPart(
        message: ActualHttpMessage,
        onPart: suspend (ActualPart) -> Unit,
    ) {
        actualParts.forEach { onPart(it) }
    }
}
