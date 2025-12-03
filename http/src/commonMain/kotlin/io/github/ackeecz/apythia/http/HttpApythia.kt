package io.github.ackeecz.apythia.http

import io.github.ackeecz.apythia.http.extension.DslExtensionConfig
import io.github.ackeecz.apythia.http.extension.DslExtensionConfigProvider
import io.github.ackeecz.apythia.http.extension.DslExtensionConfigProviderImpl
import io.github.ackeecz.apythia.http.extension.DslExtensionConfigs
import io.github.ackeecz.apythia.http.extension.DslExtensionConfigsImpl
import io.github.ackeecz.apythia.http.request.ActualHttpMessage
import io.github.ackeecz.apythia.http.request.ActualRequest
import io.github.ackeecz.apythia.http.request.ExpectedRequest
import io.github.ackeecz.apythia.http.request.HttpMethod
import io.github.ackeecz.apythia.http.request.body.ActualPart
import io.github.ackeecz.apythia.http.request.body.ExpectedBody
import io.github.ackeecz.apythia.http.request.body.ExpectedFormDataPart
import io.github.ackeecz.apythia.http.request.dsl.HttpRequestAssertion
import io.github.ackeecz.apythia.http.request.dsl.HttpRequestAssertionImpl
import io.github.ackeecz.apythia.http.request.header.ExpectedHeaders
import io.github.ackeecz.apythia.http.request.url.ExpectedQuery
import io.github.ackeecz.apythia.http.request.url.ExpectedUrl
import io.github.ackeecz.apythia.http.response.HttpResponse
import io.github.ackeecz.apythia.http.response.dsl.HttpResponseArrangement
import io.github.ackeecz.apythia.http.response.dsl.HttpResponseArrangementImpl
import io.github.ackeecz.apythia.http.util.header.getContentDispositionHeader
import io.github.ackeecz.apythia.http.util.header.lowercaseKeys
import io.kotest.assertions.fail
import io.kotest.assertions.withClue
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldEndWith

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
     * Arranges the next HTTP response that will be returned by the HTTP client. Responses must be
     * arranged such that they are returned by the HTTP client in the order they are arranged.
     */
    public fun arrangeNextResponse(arrange: HttpResponseArrangement.() -> Unit) {
        val response = HttpResponseArrangementImpl(dslExtensionConfigProvider)
            .apply(arrange)
            .httpResponse
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

    /**
     * Asserts the next HTTP request that was made by the HTTP client.
     */
    public suspend fun assertNextRequest(assertRequest: HttpRequestAssertion.() -> Unit) {
        val actualRequest = getNextActualRequest()
        val assertion = HttpRequestAssertionImpl(dslExtensionConfigProvider, actualRequest).apply(assertRequest)
        assertNextRequest(actualRequest, assertion.expectedRequest)
    }

    /**
     * Returns the next actual HTTP request made by the HTTP client.
     * The returned [ActualRequest] is then used to verify the expectations defined in [assertNextRequest].
     */
    protected abstract suspend fun getNextActualRequest(): ActualRequest

    private suspend fun assertNextRequest(actualRequest: ActualRequest, expectedRequest: ExpectedRequest) {
        with(actualRequest) {
            assertMethod(request = this, expectedMethod = expectedRequest.method)
            assertUrl(request = this, expectedUrl = expectedRequest.url)
            assertHeaders(actual = this.message.headers, expected = expectedRequest.headers)
            assertBody(actualRequest = this, expectedBody = expectedRequest.body)
        }
    }

    private fun assertMethod(request: ActualRequest, expectedMethod: HttpMethod?) {
        expectedMethod?.let { request.method.lowercase() shouldBe it.value.lowercase() }
    }

    private fun assertUrl(request: ActualRequest, expectedUrl: ExpectedUrl) {
        assertFullUrl(request, expectedUrl.url)
        assertPath(request, expectedUrl.path)
        assertPathSuffix(request, expectedUrl.pathSuffix)
        assertQuery(request, expectedUrl.query)
    }

    private fun assertFullUrl(request: ActualRequest, expectedUrl: String?) {
        expectedUrl?.let { request.url.toString() shouldBe it }
    }

    private fun assertPath(request: ActualRequest, expectedPath: String?) {
        expectedPath?.let { request.url.path shouldBe it }
    }

    private fun assertPathSuffix(request: ActualRequest, expectedPathSuffix: String?) {
        expectedPathSuffix?.let { request.url.path shouldEndWith it }
    }

    private fun assertQuery(request: ActualRequest, expectedQuery: ExpectedQuery) {
        assertQueryParameters(request, expectedQuery.parameters)
        assertMissingQueryParameters(request, expectedQuery.missingParameters)
    }

    private fun assertQueryParameters(request: ActualRequest, expectedParameters: Map<String, List<String?>>?) {
        when {
            expectedParameters == null -> return
            expectedParameters.isEmpty() -> {
                val paramNames = request.url.getQueryParameterNames()
                withClue("Expected no query parameters but were: $paramNames") {
                    paramNames.shouldBeEmpty()
                }
            }
            else -> {
                expectedParameters.forEach { (expectedParamName, expectedValues) ->
                    withClue("Query parameter '$expectedParamName' does not match expected value(s).") {
                        val actualValues = request.url.getQueryParameters(expectedParamName).map {
                            it.ifEmpty { null }
                        }
                        actualValues shouldContainExactlyInAnyOrder expectedValues
                    }
                }
            }
        }
    }

    private fun assertMissingQueryParameters(request: ActualRequest, expectedMissingParameters: Set<String>?) {
        expectedMissingParameters?.forEach { expectedMissingParamName ->
            withClue("Query parameter '$expectedMissingParamName' should be missing but is present.") {
                request.url.getQueryParameterNames()
                    .contains(expectedMissingParamName)
                    .shouldBeFalse()
            }
        }
    }

    private fun assertHeaders(actual: Map<String, List<String>>, expected: ExpectedRequest.Headers) {
        assertHeaders(actual, expected.headers)
    }

    private fun assertHeaders(actual: Map<String, List<String>>, expected: ExpectedHeaders) {
        assertHeaders(actual, expected.headers)
    }

    private fun assertHeaders(actual: Map<String, List<String>>, expected: Map<String, List<String>>?) {
        if (expected != null) {
            withClue("Headers do not match expected values. Actual headers: ${actual.toMap()}") {
                val actualLowercased = actual.lowercaseKeys()
                val expectedLowercased = expected.lowercaseKeys()
                actualLowercased.keys shouldContainAll expectedLowercased.keys
                expectedLowercased.forEach { (expectedHeaderName, expectedValues) ->
                    actualLowercased[expectedHeaderName].shouldContainExactlyInAnyOrder(expectedValues)
                }
            }
        }
    }

    private suspend fun assertBody(actualRequest: ActualRequest, expectedBody: ExpectedRequest.Body) {
        assertBody(actualRequest.message, expectedBody.body)
    }

    private suspend fun assertBody(
        actualMessage: ActualHttpMessage,
        expectedBody: ExpectedBody?,
    ) {
        if (expectedBody == null) return

        when (expectedBody) {
            is ExpectedBody.Empty -> {
                assertEmptyBody(actualMessage)
            }
            is ExpectedBody.Bytes -> {
                assertBytesBody(actualMessage, expectedBody)
            }
            is ExpectedBody.PlainText -> {
                assertPlainTextBody(actualMessage, expectedBody)
            }
            is ExpectedBody.MultipartFormData -> {
                assertMultipartFormDataBody(actualMessage, expectedBody)
            }
            is ExpectedBody.PartialMultipartFormData -> {
                assertPartialMultipartBody(actualMessage, expectedBody)
            }
        }
    }

    private fun assertEmptyBody(actualHttpMessage: ActualHttpMessage) {
        actualHttpMessage.body.shouldBeEmpty()
    }

    private fun assertBytesBody(
        actualHttpMessage: ActualHttpMessage,
        expectedBody: ExpectedBody.Bytes,
    ) {
        actualHttpMessage.body shouldBe expectedBody.value
    }

    @Suppress("DEPRECATION_ERROR")
    private fun assertPlainTextBody(
        actualHttpMessage: ActualHttpMessage,
        expectedBody: ExpectedBody.PlainText,
    ) {
        Charset.checkCharsetSupported(contentType = actualHttpMessage.contentType)
        actualHttpMessage.body.decodeToString() shouldBeEqual expectedBody.value
    }

    private suspend fun assertMultipartFormDataBody(
        actualMessage: ActualHttpMessage,
        expected: ExpectedBody.MultipartFormData,
    ) {
        assertParts(
            actualMessage = actualMessage,
            expectedParts = expected.parts,
            onUnexpectedPart = { formDataName ->
                fail("Unexpected multipart part with name '$formDataName'")
            },
        )
    }

    private suspend fun assertParts(
        actualMessage: ActualHttpMessage,
        expectedParts: List<ExpectedFormDataPart>,
        onUnexpectedPart: (formDataName: String) -> Unit,
    ) {
        val expectedPartsMutable = expectedParts.toMutableList()
        processMultiParts(actualMessage) { actualFormDataPart ->
            val matchingExpectedParts = expectedPartsMutable.filter { it.name == actualFormDataPart.name }
            if (matchingExpectedParts.isEmpty()) {
                onUnexpectedPart(actualFormDataPart.name)
            } else {
                for (index in matchingExpectedParts.indices) {
                    val expectedPart = matchingExpectedParts[index]
                    try {
                        assertPart(actualFormDataPart, expectedPart)
                        expectedPartsMutable.remove(expectedPart)
                        break
                    } catch (e: AssertionError) {
                        if (index == matchingExpectedParts.lastIndex) {
                            throw e
                        }
                    }
                }
            }
        }
        if (expectedPartsMutable.isNotEmpty()) {
            fail("Missing multipart parts: ${expectedPartsMutable.joinToString { it.name }}")
        }
    }

    private suspend fun processMultiParts(
        actualMessage: ActualHttpMessage,
        onPart: suspend (ActualFormDataPart) -> Unit
    ) {
        forEachMultipartFormDataPart(actualMessage) { part ->
            val contentDispositionHeader = getContentDispositionHeader(part.headers.toMap())
            val partName = checkNotNull(contentDispositionHeader?.name) {
                "Multipart part is missing Content-Disposition header with 'name' parameter"
            }
            val actualFormDataPart = ActualFormDataPart(
                name = partName,
                filename = contentDispositionHeader.filename,
                message = ActualHttpMessage(part.headers, part.body),
            )
            onPart(actualFormDataPart)
        }
    }

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

    private suspend fun assertPart(
        actualFormDataPart: ActualFormDataPart,
        expectedPart: ExpectedFormDataPart,
    ) {
        val actualFormDataName = actualFormDataPart.name
        val actualMessage = actualFormDataPart.message
        val actualHeaders = actualMessage.headers
        withClue("Assertion failed for form data part with name '$actualFormDataName'") {
            actualFormDataName shouldBe expectedPart.name
            actualFormDataPart.filename shouldBe expectedPart.filename
            assertFormDataPartHeaders(actualHeaders, expectedPart.headers)
            assertFormDataPartBody(actualMessage, expectedPart.body)
        }
    }

    private fun assertFormDataPartHeaders(
        actualHeaders: Map<String, List<String>>,
        expectedHeaders: ExpectedFormDataPart.Headers,
    ) {
        assertHeaders(actual = actualHeaders, expected = expectedHeaders.headers)
    }

    private suspend fun assertFormDataPartBody(
        actualMessage: ActualHttpMessage,
        expectedBody: ExpectedFormDataPart.Body,
    ) {
        assertBody(actualMessage, expectedBody.body)
    }

    private suspend fun assertPartialMultipartBody(
        actualMessage: ActualHttpMessage,
        expectedBody: ExpectedBody.PartialMultipartFormData,
    ) {
        expectedBody.parts?.let { assertParts(actualMessage, it, onUnexpectedPart = {}) }
        assertMissingParts(actualMessage, expectedBody.missingParts)
    }

    private suspend fun assertMissingParts(
        actualMessage: ActualHttpMessage,
        expectedMissingParts: Set<String>?,
    ) {
        if (expectedMissingParts != null) {
            val actualPartsNames = mutableListOf<String>()
            processMultiParts(actualMessage) { actualPartsNames.add(it.name) }
            expectedMissingParts.forEach { expectedMissingPart ->
                withClue("Multipart part '$expectedMissingPart' should be missing but is present.") {
                    actualPartsNames.contains(expectedMissingPart).shouldBeFalse()
                }
            }
        }
    }
}

private class ActualFormDataPart(
    val name: String,
    val filename: String? = null,
    val message: ActualHttpMessage,
)
