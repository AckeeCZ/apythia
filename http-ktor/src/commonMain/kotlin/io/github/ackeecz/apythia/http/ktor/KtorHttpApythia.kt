package io.github.ackeecz.apythia.http.ktor

import io.github.ackeecz.apythia.http.ExperimentalHttpApi
import io.github.ackeecz.apythia.http.HttpApythia
import io.github.ackeecz.apythia.http.extension.DslExtensionConfig
import io.github.ackeecz.apythia.http.extension.DslExtensionConfigs
import io.github.ackeecz.apythia.http.request.ActualHttpMessage
import io.github.ackeecz.apythia.http.request.ActualRequest
import io.github.ackeecz.apythia.http.request.body.ActualPart
import io.github.ackeecz.apythia.http.response.HttpResponse
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpStatusCode
import io.ktor.http.cio.CIOMultipartDataBase
import io.ktor.http.content.PartData
import io.ktor.http.content.TextContent
import io.ktor.http.content.forEachPart
import io.ktor.util.toMap
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.InternalAPI
import io.ktor.utils.io.toByteArray
import kotlinx.io.readByteArray
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.coroutines.EmptyCoroutineContext

/**
 * [HttpApythia] implementation for Ktor HTTP client. After calling [beforeEachTest] you can retrieve
 * [mockEngine] to pass it to your Ktor HTTP client instance. This allows [KtorHttpApythia] to mock
 * responses and assert requests.
 *
 * For more information check [HttpApythia] documentation.
 *
 * @param dslExtensionConfigs DSL for adding [DslExtensionConfig]s.
 */
@OptIn(ExperimentalAtomicApi::class)
public class KtorHttpApythia(
    dslExtensionConfigs: DslExtensionConfigs.() -> Unit = {},
) : HttpApythia(dslExtensionConfigs) {

    private var _mockEngine: MockEngine? = null
    public val mockEngine: MockEngine
        get() = _mockEngine ?: error("Mock engine is not initialized. Did you call beforeEachTest()?")

    private var requestHistoryIndex = 0

    /**
     * [ActualRequest]s already converted by [mockNextResponseProvider], keyed by the request they
     * were converted from. Converting reads the request body, which is not repeatable for streamed
     * bodies (e.g. `setBody(ByteReadChannel)`), so a request converted at the response mocking time
     * has to be reused by [getNextActualRequest] instead of being converted again.
     */
    private val actualRequests = AtomicReference<Map<HttpRequestData, ActualRequest>>(emptyMap())

    override fun beforeEachTest() {
        // Dummy response, because Ktor requires to set some response handler
        _mockEngine = MockEngine { respond(status = HttpStatusCode.OK, content = "") }
        mockEngine.config.requestHandlers.clear()
        mockEngine.config.reuseHandlers = false
        actualRequests.store(emptyMap())
    }

    override fun afterEachTest() {
        mockEngine.close()
        _mockEngine = null
        requestHistoryIndex = 0
        actualRequests.store(emptyMap())
    }

    override fun mockNextResponse(response: HttpResponse) {
        mockEngine.config.addHandler { respond(response) }
    }

    @ExperimentalHttpApi
    override fun mockNextResponseProvider(provideResponse: (ActualRequest) -> HttpResponse) {
        mockEngine.config.addHandler { request ->
            respond(provideResponse(request.toCachedActualRequest()))
        }
    }

    private fun MockRequestHandleScope.respond(response: HttpResponse): HttpResponseData {
        return respond(
            content = response.body,
            status = HttpStatusCode.fromValue(response.statusCode),
            headers = Headers.build {
                response.headers.forEach { (key, values) ->
                    appendAll(key, values)
                }
            },
        )
    }

    override suspend fun getNextActualRequest(): ActualRequest {
        val request = mockEngine.requestHistory[requestHistoryIndex++]
        return actualRequests.load()[request] ?: request.toActualRequest()
    }

    private suspend fun HttpRequestData.toCachedActualRequest(): ActualRequest {
        val actualRequest = toActualRequest()
        while (true) {
            val current = actualRequests.load()
            if (actualRequests.compareAndSet(current, current + (this to actualRequest))) {
                return actualRequest
            }
        }
    }

    private suspend fun HttpRequestData.toActualRequest(): ActualRequest {
        val headers = headers.toMap().toMutableMap()
        body.contentType?.let { headers["Content-Type"] = listOf(it.toString()) }
        return ActualRequest(
            method = method.value,
            url = url.toString(),
            headers = headers,
            body = body.toByteArray(),
        )
    }

    @ExperimentalHttpApi
    @OptIn(InternalAPI::class)
    override suspend fun forEachMultipartFormDataPart(
        message: ActualHttpMessage,
        onPart: suspend (ActualPart) -> Unit,
    ) {
        val channel = ByteReadChannel(message.body)
        val result = CIOMultipartDataBase(
            coroutineContext = EmptyCoroutineContext,
            channel = channel,
            contentType = checkNotNull(message.contentType),
            contentLength = message.contentLength.toLong(),
        )
        result.forEachPart { ktorPart ->
            val contentType = ktorPart.contentType
            val partBody = when (ktorPart) {
                is PartData.FormItem -> TextContent(ktorPart.value, contentType ?: ContentType.Text.Plain).toByteArray()
                is PartData.FileItem -> ktorPart.provider().toByteArray()
                is PartData.BinaryItem -> ktorPart.provider().readByteArray()
                is PartData.BinaryChannelItem -> ktorPart.provider().toByteArray()
            }
            val headers = ktorPart.headers.toMap().toMutableMap()
            if (contentType != null) {
                headers["Content-Type"] = listOf(contentType.toString())
            }
            onPart(ActualPart(headers = headers, body = partBody))
        }
    }
}
