package io.github.ackeecz.apythia.http.okhttp

import io.github.ackeecz.apythia.http.ExperimentalHttpApi
import io.github.ackeecz.apythia.http.HttpApythia
import io.github.ackeecz.apythia.http.request.ActualHttpMessage
import io.github.ackeecz.apythia.http.request.ActualRequest
import io.github.ackeecz.apythia.http.request.body.ActualPart
import io.github.ackeecz.apythia.http.response.HttpResponse
import kotlinx.serialization.json.Json
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.Headers
import okio.Buffer

/**
 * [HttpApythia] implementation backed by OkHttp's mock web server. After calling [beforeEachTest]
 * you can retrieve mock web server's URL using [getMockWebServerUrl] to pass it to your HTTP client.
 * This allows [OkHttpHttpApythia] to arrange responses and assert requests.
 *
 * For more information check [HttpApythia] documentation.
 *
 * @param json JSON serializer instance to use for encoding/decoding JSON bodies of responses and
 * requests.
 */
public class OkHttpHttpApythia(json: Json = Json) : HttpApythia(json) {

    private var _mockWebServer: MockWebServer? = null
    private val mockWebServer: MockWebServer
        get() = _mockWebServer ?: error("Mock web server is not initialized. Did you call beforeEachTest()?")

    public fun getMockWebServerUrl(path: String = "/"): String {
        return mockWebServer.url(path).toString()
    }

    override fun beforeEachTest() {
        _mockWebServer = MockWebServer()
        mockWebServer.start()
    }

    override fun afterEachTest() {
        mockWebServer.close()
        _mockWebServer = null
    }

    override fun arrangeNextResponse(response: HttpResponse) {
        val headersBuilder = Headers.Builder()
        response.headers.toList().forEach { (key, values) ->
            values.forEach { value ->
                headersBuilder.add(key, value)
            }
        }
        val bodyBuffer = Buffer().use { it.write(response.body) }
        val mockedResponse = MockResponse.Builder()
            .code(response.statusCode)
            .headers(headersBuilder.build())
            .body(bodyBuffer)
            .build()
        mockWebServer.enqueue(mockedResponse)
    }

    override fun getNextActualRequest(): ActualRequest {
        TODO()
    }

    @ExperimentalHttpApi
    override suspend fun forEachMultipartFormDataPart(
        message: ActualHttpMessage,
        onPart: suspend (ActualPart) -> Unit,
    ) {
        TODO()
    }
}
