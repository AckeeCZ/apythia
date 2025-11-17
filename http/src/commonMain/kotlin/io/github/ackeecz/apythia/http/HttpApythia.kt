package io.github.ackeecz.apythia.http

import io.github.ackeecz.apythia.http.response.HttpResponse
import io.github.ackeecz.apythia.http.response.dsl.HttpResponseArrangement
import io.github.ackeecz.apythia.http.response.dsl.HttpResponseArrangementImpl
import kotlinx.serialization.json.Json

/**
 * Base class for HTTP API testing. Provides methods for arranging HTTP responses and asserting
 * HTTP requests. [HttpApythia] serves as an abstraction over complexities of mocking responses and
 * asserting requests using nice DSLs that are easy to use and understand. The other big advantage
 * of this abstraction is that it is completely independent of any HTTP client implementation,
 * which is ideal for using it in tests to make them as black-box as possible.
 *
 * There are particular [HttpApythia] implementations for popular HTTP clients like Ktor or OkHttp.
 * So if you use a HTTP client in your application that is supported by Apythia library, you can
 * just include an appropriate artifact (e.g. `http-ktor`), which provides an implementation of
 * [HttpApythia] backed by that HTTP client mocking capabilities.
 *
 * Since [HttpApythia] is abstract, you can extend it to create your own implementation for a HTTP
 * client that is not supported by Apythia library. When you do this, the most important thing is
 * to implement [beforeEachTest] and [afterEachTest] methods properly. You have to initialize
 * everything needed per test in [beforeEachTest] instead of relying on the constructor, because
 * [HttpApythia] is designed to work no matter if the client creates its instance per test or per
 * a test class/suite. You can check existing implementations for a reference.
 *
 * // TODO Provide example of recommended usage in test classes
 *
 * @param json JSON serializer instance to use for encoding/decoding JSON bodies of responses and
 * requests.
 */
public abstract class HttpApythia(private val json: Json = Json) {

    /**
     * Needs to be called before each test to initialize everything needed for HTTP client mocking.
     *
     * Implementations of [HttpApythia] have to initialize everything needed per test in this method
     * instead of relying on the constructor, because [HttpApythia] is designed to work no matter if
     * the client creates its instance per test or per a test class/suite.
     */
    public abstract fun beforeEachTest()

    /**
     * Needs to be called after each test to clean up everything initialized in [beforeEachTest].
     */
    public abstract fun afterEachTest()

    /**
     * Arranges the next HTTP response that will be returned by the HTTP client. Responses must be
     * arranged such that they are returned by the HTTP client in the order they are arranged.
     */
    public fun arrangeNextResponse(arrange: HttpResponseArrangement.() -> Unit) {
        val response = HttpResponseArrangementImpl(json).apply(arrange).httpResponse
        arrangeNextResponse(response)
    }

    /**
     * Same as [arrangeNextResponse] with DSL parameter but arranges a 200 response with an empty body.
     */
    public fun arrangeNext200Response() {
        arrangeNextResponse {}
    }

    /**
     * Same as public [arrangeNextResponse].
     *
     * @param response HTTP response that needs to be arranged to the particular HTTP client mock
     * implementation to be returned when making HTTP requests.
     */
    protected abstract fun arrangeNextResponse(response: HttpResponse)
}
