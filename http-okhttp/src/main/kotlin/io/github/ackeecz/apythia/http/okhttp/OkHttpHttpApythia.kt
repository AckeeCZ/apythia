package io.github.ackeecz.apythia.http.okhttp

import io.github.ackeecz.apythia.http.ExperimentalHttpApi
import io.github.ackeecz.apythia.http.HttpApythia
import io.github.ackeecz.apythia.http.extension.DslExtensionConfig
import io.github.ackeecz.apythia.http.extension.DslExtensionConfigs
import io.github.ackeecz.apythia.http.request.ActualHttpMessage
import io.github.ackeecz.apythia.http.request.ActualRequest
import io.github.ackeecz.apythia.http.request.body.ActualPart
import io.github.ackeecz.apythia.http.response.HttpResponse
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import mockwebserver3.RecordedRequest
import okhttp3.Headers
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartReader
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer

/**
 * [HttpApythia] implementation backed by OkHttp's mock web server. After calling [beforeEachTest]
 * you can retrieve mock web server's URL using [getMockWebServerUrl] to pass it to your HTTP client.
 * This allows [OkHttpHttpApythia] to mock responses and assert requests.
 *
 * For more information check [HttpApythia] documentation.
 *
 * @param dslExtensionConfigs DSL for adding [DslExtensionConfig]s.
 */
public class OkHttpHttpApythia(
    dslExtensionConfigs: DslExtensionConfigs.() -> Unit = {},
) : HttpApythia(dslExtensionConfigs) {

    private var _mockWebServer: MockWebServer? = null
    private val mockWebServer: MockWebServer
        get() = _mockWebServer.orNotInitialized("Mock web server")

    private var _dispatcher: ResponseProviderDispatcher? = null
    private val dispatcher: ResponseProviderDispatcher
        get() = _dispatcher.orNotInitialized("Dispatcher")

    private fun <T : Any> T?.orNotInitialized(name: String): T {
        return this ?: error("$name is not initialized. Did you call beforeEachTest()?")
    }

    public fun getMockWebServerUrl(path: String = "/"): String {
        return mockWebServer.url(path).toString()
    }

    override fun beforeEachTest() {
        _mockWebServer = MockWebServer()
        _dispatcher = ResponseProviderDispatcher()
        mockWebServer.dispatcher = dispatcher
        mockWebServer.start()
    }

    override fun afterEachTest() {
        mockWebServer.close()
        _mockWebServer = null
        _dispatcher = null
    }

    override fun mockNextResponse(response: HttpResponse) {
        val mockedResponse = response.toMockResponse()
        dispatcher.enqueue { mockedResponse }
    }

    @ExperimentalHttpApi
    override fun mockNextResponseProvider(provideResponse: (ActualRequest) -> HttpResponse) {
        dispatcher.enqueue { request ->
            provideResponse(request.toActualRequest()).toMockResponse()
        }
    }

    private fun HttpResponse.toMockResponse(): MockResponse {
        val headersBuilder = Headers.Builder()
        headers.toList().forEach { (key, values) ->
            values.forEach { value ->
                headersBuilder.add(key, value)
            }
        }
        val bodyBuffer = Buffer().use { it.write(body) }
        return MockResponse.Builder()
            .code(statusCode)
            .headers(headersBuilder.build())
            .body(bodyBuffer)
            .build()
    }

    override suspend fun getNextActualRequest(): ActualRequest {
        return mockWebServer.takeRequest().toActualRequest()
    }

    private fun RecordedRequest.toActualRequest(): ActualRequest {
        return ActualRequest(
            method = method,
            url = url.toString(),
            headers = headers.toMultimap(),
            body = body?.toByteArray() ?: byteArrayOf(),
        )
    }

    @ExperimentalHttpApi
    override suspend fun forEachMultipartFormDataPart(
        message: ActualHttpMessage,
        onPart: suspend (ActualPart) -> Unit,
    ) {
        message.body.toResponseBody(message.contentType?.toMediaType())
            .let { MultipartReader(it) }
            .use { reader ->
                var nextPart = reader.nextPart()
                while (nextPart != null) {
                    val part = ActualPart(
                        headers = nextPart.headers.toMultimap(),
                        body = nextPart.body.readByteArray(),
                    )
                    onPart(part)
                    nextPart = reader.nextPart()
                }
            }
    }
}
