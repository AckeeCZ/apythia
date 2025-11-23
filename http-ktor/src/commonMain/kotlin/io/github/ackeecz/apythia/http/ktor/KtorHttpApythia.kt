package io.github.ackeecz.apythia.http.ktor

import io.github.ackeecz.apythia.http.ExperimentalHttpApi
import io.github.ackeecz.apythia.http.HttpApythia
import io.github.ackeecz.apythia.http.request.ActualHttpMessage
import io.github.ackeecz.apythia.http.request.ActualRequest
import io.github.ackeecz.apythia.http.request.body.ActualPart
import io.github.ackeecz.apythia.http.response.HttpResponse
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.Headers
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.Json

/**
 * [HttpApythia] implementation for Ktor HTTP client. After calling [beforeEachTest] you can retrieve
 * [mockEngine] to pass it to your Ktor HTTP client instance. This allows [KtorHttpApythia] to arrange
 * responses and assert requests.
 *
 * For more information check [HttpApythia] documentation.
 *
 * @param json JSON serializer instance to use for encoding/decoding JSON bodies of responses and
 * requests.
 */
public class KtorHttpApythia(json: Json = Json) : HttpApythia(json) {

    private var _mockEngine: MockEngine? = null
    public val mockEngine: MockEngine
        get() = _mockEngine ?: error("Mock engine is not initialized. Did you call beforeEachTest()?")

    override fun beforeEachTest() {
        // Dummy response, because Ktor requires to set some response handler
        _mockEngine = MockEngine { respond(status = HttpStatusCode.OK, content = "") }
        mockEngine.config.requestHandlers.clear()
        mockEngine.config.reuseHandlers = false
    }

    override fun afterEachTest() {
        mockEngine.close()
        _mockEngine = null
    }

    override fun arrangeNextResponse(response: HttpResponse) {
        mockEngine.config.addHandler {
            respond(
                content = response.body,
                status = HttpStatusCode.fromValue(response.statusCode),
                headers = Headers.build {
                    response.headers.forEach { (key, values) ->
                        appendAll(key, values)
                    }
                },
            )
        }
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
