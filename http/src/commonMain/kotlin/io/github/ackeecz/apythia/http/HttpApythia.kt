package io.github.ackeecz.apythia.http

import io.github.ackeecz.apythia.http.extension.DslExtensionConfig
import io.github.ackeecz.apythia.http.extension.DslExtensionConfigProvider
import io.github.ackeecz.apythia.http.extension.DslExtensionConfigProviderImpl
import io.github.ackeecz.apythia.http.extension.DslExtensionConfigs
import io.github.ackeecz.apythia.http.extension.DslExtensionConfigsImpl
import io.github.ackeecz.apythia.http.request.ActualHttpMessage
import io.github.ackeecz.apythia.http.request.ActualRequest
import io.github.ackeecz.apythia.http.request.body.ActualPart
import io.github.ackeecz.apythia.http.request.dsl.HttpRequestAssertion
import io.github.ackeecz.apythia.http.request.dsl.HttpRequestAssertionImpl
import io.github.ackeecz.apythia.http.response.HttpResponse
import io.github.ackeecz.apythia.http.response.dsl.HttpResponseMockBuilder
import io.github.ackeecz.apythia.http.response.dsl.HttpResponseMockBuilderImpl

/**
 * Base class for HTTP API testing. It provides methods for mocking HTTP responses and asserting
 * HTTP requests. [HttpApythia] abstracts away the complexities of mocking responses and verifying
 * requests, offering an intuitive DSL that is easy to read and use. Another major benefit of this
 * abstraction is that it is completely independent of any HTTP client implementation, which makes
 * it ideal for black-box testing.
 *
 * The Apythia library includes specific [HttpApythia] implementations for popular HTTP clients
 * such as Ktor and OkHttp. If your application uses a supported client, you can simply add the
 * corresponding module (e.g. http-ktor), which provides an [HttpApythia] implementation backed
 * by that client's mocking capabilities.
 *
 * Since [HttpApythia] is abstract, you can extend it to build your own implementation for any
 * unsupported HTTP client. When doing so, the most important requirement is to implement
 * [beforeEachTest] and [afterEachTest] correctly. All per-test initialization must happen inside
 * [beforeEachTest] rather than in the constructor, because [HttpApythia] is designed to work
 * regardless of whether a new instance is created per test or per test class/suite. Existing
 * implementations can serve as a helpful reference.
 *
 * Responses can be mocked either statically by [mockNextResponse], or dynamically by
 * [mockNextDynamicResponse] and [mockNextDynamicResponses]. A dynamic response is built at the time
 * the request arrives and has access to that request, which makes it possible to derive the response
 * from the request data. This is especially useful for concurrent requests, where the order in which
 * they arrive is not deterministic and so a statically mocked response can't be reliably paired with
 * a particular request.
 *
 * By default, [HttpApythia] is also independent of any serialization library, including Kotlinx
 * Serialization. If you want DSL extensions for Kotlinx Serialization’s JSON API, you can include
 * the http-ext-json-kotlinx-serialization artifact. Creating your own DSL extensions is equally
 * easy—for example, to integrate a different serialization library or to add custom mocking or
 * assertion helpers. Refer to the existing DSL extensions for examples.
 *
 * A typical usage pattern looks like this:
 *
 * ```kotlin
 * private lateinit var httpApythia: HttpApythia
 * private lateinit var underTest: RemoteDataSource
 *
 * class RemoteDataSourceImplTest : FunSpec({
 *
 *     val ktorHttpApythia = KtorHttpApythia().also { httpApythia = it }
 *
 *     beforeEach {
 *         ktorHttpApythia.beforeEachTest()
 *         val httpClient = HttpClient(ktorHttpApythia.mockEngine)
 *         underTest = RemoteDataSourceImpl(httpClient)
 *     }
 *
 *     afterEach {
 *         ktorHttpApythia.afterEachTest()
 *     }
 *
 *     ...
 * }
 * ```
 * The key idea is to instantiate the concrete [HttpApythia] implementation during your test setup
 * and use that concrete type only when configuring your SUT (e.g., in Kotest’s beforeEach).
 * During the tests themselves, interact with Apythia **only** through the abstract [HttpApythia]
 * interface, keeping your tests fully decoupled from the underlying HTTP client.
 *
 * @param dslExtensionConfigs DSL for adding [DslExtensionConfig]s.
 */
public abstract class HttpApythia(
    dslExtensionConfigs: DslExtensionConfigs.() -> Unit,
) {

    private val dslExtensionConfigProvider: DslExtensionConfigProvider

    init {
        val configs = DslExtensionConfigsImpl().apply(dslExtensionConfigs).configs
        dslExtensionConfigProvider = DslExtensionConfigProviderImpl(configs)
    }

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
     * Mocks the next HTTP response that will be returned by the HTTP client. Responses must be
     * mocked such that they are returned by the HTTP client in the order they are mocked.
     */
    public fun mockNextResponse(mock: HttpResponseMockBuilder.() -> Unit) {
        mockNextResponse(buildResponse(mock))
    }

    private fun buildResponse(mock: HttpResponseMockBuilder.() -> Unit): HttpResponse {
        return HttpResponseMockBuilderImpl(dslExtensionConfigProvider)
            .apply(mock)
            .httpResponse
    }

    /**
     * Mocks the next HTTP response like [mockNextResponse], but the [mock] block runs when the
     * request arrives and receives that request, so the response can be derived from the request
     * data (e.g. from an id query parameter). This makes mocking of concurrent requests
     * order-independent, because each response is built from its own request.
     *
     * Dynamic responses are consumed in the mocking order together with the static ones mocked by
     * [mockNextResponse] and each call answers exactly one request. A fresh
     * [HttpResponseMockBuilder] is used for each arriving request.
     *
     * The [mock] block runs on the thread of the underlying HTTP client mock at the request time,
     * so an exception thrown inside of it fails that request instead of this method call.
     */
    @ExperimentalHttpApi
    public fun mockNextDynamicResponse(mock: HttpResponseMockBuilder.(request: ActualRequest) -> Unit) {
        mockNextResponseProvider { request ->
            buildResponse { mock(request) }
        }
    }

    /**
     * Same as [mockNextDynamicResponse], but mocks the [mock] block [count] times, so [count]
     * requests (e.g. concurrent ones) are each answered by a response built from their own
     * request data.
     *
     * @param count How many times the [mock] block is mocked. Must be greater than 0.
     * @throws IllegalArgumentException If [count] is lower than 1.
     */
    @ExperimentalHttpApi
    public fun mockNextDynamicResponses(
        count: Int,
        mock: HttpResponseMockBuilder.(request: ActualRequest) -> Unit,
    ) {
        require(count > 0) { "count must be greater than 0, but was $count" }
        repeat(count) {
            mockNextDynamicResponse(mock)
        }
    }

    /**
     * Implementation hook for dynamic responses mocked by [mockNextDynamicResponse]. Implementations
     * have to register exactly one positional (one-shot) response handler that converts an arriving
     * request to [ActualRequest], calls [provideResponse] with it and returns the resulting
     * [HttpResponse]. Handlers must be consumed in the same order they are registered, together
     * with the ones registered by [mockNextResponse].
     *
     * The default implementation does not support dynamic responses and throws
     * [IllegalStateException] at the mocking time.
     *
     * @param provideResponse Builds the [HttpResponse] for the arriving [ActualRequest].
     * @throws IllegalStateException If the implementation does not support dynamic responses.
     */
    @ExperimentalHttpApi
    protected open fun mockNextResponseProvider(provideResponse: (ActualRequest) -> HttpResponse) {
        error("${this::class.simpleName} does not support dynamic responses")
    }

    /**
     * Same as [mockNextResponse] with DSL parameter but mocks a 200 response with an empty body.
     */
    public fun mockNext200Response() {
        mockNextResponse {}
    }

    /**
     * Same as public [mockNextResponse].
     *
     * @param response HTTP response that needs to be mocked to the particular HTTP client mock
     * implementation to be returned when making HTTP requests.
     */
    protected abstract fun mockNextResponse(response: HttpResponse)

    /**
     * Asserts the next HTTP request that was made by the HTTP client.
     */
    public suspend fun assertNextRequest(assertRequest: suspend HttpRequestAssertion.() -> Unit) {
        HttpRequestAssertionImpl(
            configProvider = dslExtensionConfigProvider,
            actualRequest = getNextActualRequest(),
            collectMultipartParts = { actualMessage ->
                mutableListOf<ActualPart>().also { list ->
                    forEachMultipartFormDataPart(actualMessage) { list.add(it) }
                }
            },
        ).assertRequest()
    }

    /**
     * Returns the next actual HTTP request made by the HTTP client.
     * The returned [ActualRequest] is then used to verify the expectations defined in [assertNextRequest].
     */
    protected abstract suspend fun getNextActualRequest(): ActualRequest

    /**
     * Parses and iterates over all multipart/form-data parts in the given HTTP [message].
     *
     * @param message The HTTP message containing the multipart/form-data parts.
     * @param onPart A callback invoked for each multipart/form-data part.
     */
    @ExperimentalHttpApi
    protected abstract suspend fun forEachMultipartFormDataPart(
        message: ActualHttpMessage,
        onPart: suspend (ActualPart) -> Unit,
    )
}
