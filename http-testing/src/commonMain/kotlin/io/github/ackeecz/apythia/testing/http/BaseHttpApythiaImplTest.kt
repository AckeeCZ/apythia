package io.github.ackeecz.apythia.testing.http

import io.github.ackeecz.apythia.http.HttpApythia
import io.github.ackeecz.apythia.http.request.HttpMethod
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/**
 * Base class for testing [HttpApythia] implementations.
 */
public abstract class BaseHttpApythiaImplTest<Sut : HttpApythia> : FunSpec() {

    private val underTest: Sut
    private lateinit var remoteDataSource: RemoteDataSource

    init {
        underTest = createSut()
        beforeEach {
            underTest.beforeEachTest()
            remoteDataSource = createRemoteDataSource(underTest)
        }
        afterEach {
            underTest.afterEachTest()
        }

        mockingTests()
        dynamicMockingTests()
        actualRequestTests()
        multipartFormDataTests()
    }

    protected abstract fun createSut(): Sut

    protected abstract fun createRemoteDataSource(sut: Sut): RemoteDataSource

    private fun mockingTests() {
        context("mocking") {
            test("set status code") {
                val expectedCode = 305
                underTest.mockNextResponse {
                    statusCode(expectedCode)
                }

                val actual = remoteDataSource.getMockedResponse()

                actual.statusCode shouldBe expectedCode
            }

            test("set empty headers") {
                underTest.mockNextResponse {}

                val actual = remoteDataSource.getMockedResponse()

                actual.headers.shouldBeEmpty()
            }

            test("set headers") {
                val expected = mapOf(
                    "X-Custom-Header" to listOf("value"),
                    "X-Another-Header" to listOf("first-value", "second-value"),
                    "Content-Type" to listOf("application/json"),
                )
                underTest.mockNextResponse {
                    headers {
                        expected.forEach { (name, values) -> headers(name, values) }
                    }
                }

                val actual = remoteDataSource.getMockedResponse()

                actual.headers.lowercaseKeys() shouldBe expected.lowercaseKeys()
            }

            test("set empty body") {
                underTest.mockNextResponse {}

                val actual = remoteDataSource.getMockedResponse()

                actual.body.shouldBeEmpty()
            }

            test("set body") {
                val expected = byteArrayOf(1, 2, 3)
                underTest.mockNextResponse {
                    bytesBody(expected, contentType = null)
                }

                val actual = remoteDataSource.getMockedResponse()

                actual.body shouldBe expected
            }

            test("mock multiple responses") {
                val expectedCode = 305
                val expectedBody = byteArrayOf(1, 2, 3)
                underTest.mockNextResponse {
                    statusCode(expectedCode)
                }
                underTest.mockNextResponse {
                    bytesBody(expectedBody, contentType = null)
                }

                remoteDataSource.getMockedResponse().statusCode shouldBe expectedCode
                remoteDataSource.getMockedResponse().body shouldBe expectedBody
            }

            test("mock 200 response with empty body") {
                underTest.mockNext200Response()
                val expectedCode = 200

                val actual = remoteDataSource.getMockedResponse()

                actual.statusCode shouldBe expectedCode
                actual.body.shouldBeEmpty()
            }
        }
    }

    private fun dynamicMockingTests() {
        context("dynamic mocking") {
            test("build response from a query parameter") {
                val expected = "abc"
                underTest.mockNextDynamicResponse { request ->
                    plainTextBody(request.queryParameter("id")!!)
                }

                val actual = remoteDataSource.getMockedResponse(queryParams = mapOf("id" to expected))

                actual.body.decodeToString() shouldBe expected
            }

            test("echo request method and path into response headers") {
                val expectedPath = "/dynamic/path"
                underTest.mockNextDynamicResponse { request ->
                    headers {
                        header(METHOD_HEADER, request.method)
                        header(PATH_HEADER, request.path)
                    }
                }

                val actual = remoteDataSource.sendPostRequest(url = "${remoteDataSource.baseUrl}dynamic/path")

                actual.singleHeader(METHOD_HEADER)?.lowercase() shouldBe "post"
                actual.singleHeader(PATH_HEADER) shouldBe expectedPath
            }

            test("echo a request header and the request body") {
                val expectedId = "42"
                val expectedBody = byteArrayOf(1, 2, 3)
                underTest.mockNextDynamicResponse { request ->
                    headers {
                        header(ID_HEADER, request.headers.lowercaseKeys().getValue(ID_HEADER.lowercase()).single())
                    }
                    bytesBody(request.body, contentType = null)
                }

                val actual = remoteDataSource.sendPostRequest(
                    headers = mapOf(ID_HEADER to expectedId),
                    body = expectedBody,
                )

                actual.singleHeader(ID_HEADER) shouldBe expectedId
                actual.body shouldBe expectedBody
            }

            test("static and dynamic mocks are consumed in mocking order") {
                underTest.mockNextResponse { statusCode(201) }
                underTest.mockNextDynamicResponse { statusCode(202) }
                underTest.mockNextResponse { statusCode(203) }

                remoteDataSource.getMockedResponse().statusCode shouldBe 201
                remoteDataSource.getMockedResponse().statusCode shouldBe 202
                remoteDataSource.getMockedResponse().statusCode shouldBe 203
            }

            test("each enqueued dynamic mock answers exactly one request") {
                underTest.mockNextDynamicResponses(count = 2) { request ->
                    statusCode(request.queryParameter("code")!!.toInt())
                }

                remoteDataSource.getMockedResponse(mapOf("code" to "201")).statusCode shouldBe 201
                remoteDataSource.getMockedResponse(mapOf("code" to "202")).statusCode shouldBe 202
            }

            test("concurrent requests receive responses matching their own query parameter") {
                val expectedIds = listOf("video-1", "video-2", "video-3")
                underTest.mockNextDynamicResponses(count = expectedIds.size) { request ->
                    plainTextBody(request.queryParameter("id")!!)
                }

                val actual = expectedIds.sendConcurrently { id ->
                    remoteDataSource.getMockedResponse(mapOf("id" to id))
                }

                actual.map { it.body.decodeToString() } shouldBe expectedIds
            }

            test("concurrent requests receive responses matching their own body") {
                val expectedIds = listOf("video-1", "video-2", "video-3")
                underTest.mockNextDynamicResponses(count = expectedIds.size) { request ->
                    bytesBody(request.body, contentType = null)
                }

                val actual = expectedIds.sendConcurrently { id ->
                    remoteDataSource.sendPostRequest(body = id.encodeToByteArray())
                }

                actual.map { it.body.decodeToString() } shouldBe expectedIds
            }
        }
    }

    private fun actualRequestTests() {
        context("actual request") {
            test("POST method") {
                underTest.mockNext200Response()

                remoteDataSource.sendPostRequest()

                underTest.assertNextRequest {
                    method(HttpMethod.POST)
                }
            }

            test("URL") {
                underTest.mockNext200Response()
                val expectedUrl = "${remoteDataSource.baseUrl}/url/test"

                remoteDataSource.sendPostRequest(url = expectedUrl)

                underTest.assertNextRequest {
                    url { url(expectedUrl) }
                }
            }

            test("URL encoding") {
                underTest.mockNext200Response()
                val actualPath = "pa t+h"
                val expectedPath = "/$actualPath"
                val actualQueryParams = mapOf(
                    "param 1" to "value 1",
                    "param+2" to "value+2",
                )
                val expectedQuery = actualQueryParams.entries.joinToString("&") { "${it.key}=${it.value}" }
                val expectedUrl = "${remoteDataSource.baseUrl}$actualPath?$expectedQuery"

                remoteDataSource.testUrlEncoding(actualPath, actualQueryParams)

                underTest.assertNextRequest {
                    url {
                        actualUrl shouldBe expectedUrl
                        url(expectedUrl)
                        path(expectedPath)
                        pathSuffix(expectedPath)
                        query {
                            actualQueryParams.forEach { (key, value) ->
                                parameter(key, value)
                            }
                        }
                    }
                }
            }

            test("headers") {
                underTest.mockNext200Response()
                val mimeType = "text/plain"
                val charsetParam = "charset" to "UTF-8"
                val expected = mapOf(
                    "X-Custom-Header" to "value",
                    "X-Another-Header" to "first-value",
                    "Content-Type" to "$mimeType; ${charsetParam.first}=${charsetParam.second}",
                )

                remoteDataSource.sendPostRequest(headers = expected)

                underTest.assertNextRequest {
                    headers {
                        expected.forEach { (key, value) ->
                            header(key, value)
                        }
                    }
                }
            }

            test("basic body (anything except multipart/*)") {
                underTest.mockNext200Response()
                val expected = byteArrayOf(1, 2, 3)

                remoteDataSource.sendPostRequest(body = expected)

                underTest.assertNextRequest {
                    body { bytes(expected) }
                }
            }

            test("send multiple requests and then assert them") {
                underTest.mockNext200Response()
                underTest.mockNext200Response()
                val firstExpectedBody = byteArrayOf(1, 2, 3)
                val secondExpectedBody = byteArrayOf(4, 5, 6)

                remoteDataSource.sendPostRequest(body = firstExpectedBody)
                remoteDataSource.sendPostRequest(body = secondExpectedBody)

                underTest.assertNextRequest {
                    body { bytes(firstExpectedBody) }
                }
                underTest.assertNextRequest {
                    body { bytes(secondExpectedBody) }
                }
            }

            test("send request and assert it afterwards multiple times") {
                underTest.mockNext200Response()
                underTest.mockNext200Response()
                val firstExpectedBody = byteArrayOf(1, 2, 3)
                val secondExpectedBody = byteArrayOf(4, 5, 6)

                remoteDataSource.sendPostRequest(body = firstExpectedBody)
                underTest.assertNextRequest {
                    body { bytes(firstExpectedBody) }
                }
                remoteDataSource.sendPostRequest(body = secondExpectedBody)
                underTest.assertNextRequest {
                    body { bytes(secondExpectedBody) }
                }
            }
        }
    }

    private fun multipartFormDataTests() {
        context("multipart/form-data") {
            test("process parts") {
                underTest.mockNext200Response()
                val expectedHeaders = mapOf(
                    "X-Custom-Header" to "value",
                    "Content-Type" to "image/jpeg",
                )
                val expectedParts = mapOf(
                    "part1" to byteArrayOf(1, 2, 3),
                    "part2" to byteArrayOf(4, 5, 6),
                    "part3" to byteArrayOf(7, 8, 9),
                )

                remoteDataSource.sendMultipartRequest(
                    eachPartHeaders = expectedHeaders,
                    partNamesToBodies = expectedParts
                )

                underTest.assertNextRequest {
                    body {
                        multipartFormData {
                            expectedParts.forEach { (name, body) ->
                                part(name) {
                                    headers {
                                        expectedHeaders.forEach { (headerName, headerValue) ->
                                            header(headerName, headerValue)
                                        }
                                    }
                                    body { bytes(body) }
                                }
                            }
                        }
                    }
                }
            }

            // From practical POV this is not really used in real world, even though HTTP standard allows
            // that. Since assertion DSL allows nesting multipart/form-data, it is a good idea to test it,
            // but if this was ever problematic to implement for some RemoteDataSource implementation,
            // we could just drop this test completely to make things easier or at least limit the test
            // only to non-problematic HttpApythia implementations.
            test("process nested parts") {
                underTest.mockNext200Response()
                val expectedParts = mapOf(
                    "part1" to mapOf(
                        "nested1" to byteArrayOf(1, 2, 3),
                        "nested2" to byteArrayOf(4, 5, 6),
                    ),
                )

                remoteDataSource.sendNestedMultipartRequest(expectedParts)

                underTest.assertNextRequest {
                    body {
                        multipartFormData {
                            expectedParts.forEach { (name, nestedParts) ->
                                part(name) {
                                    body {
                                        multipartFormData {
                                            nestedParts.forEach { (nestedName, nestedBody) ->
                                                part(nestedName) {
                                                    body { bytes(nestedBody) }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun MockedResponse.singleHeader(name: String): String? {
        return headers.lowercaseKeys()[name.lowercase()]?.single()
    }

    private suspend fun <T> List<T>.sendConcurrently(
        send: suspend (T) -> MockedResponse,
    ): List<MockedResponse> = coroutineScope {
        map { item -> async { send(item) } }.awaitAll()
    }

    private companion object {

        private const val METHOD_HEADER = "X-Method"
        private const val PATH_HEADER = "X-Path"
        private const val ID_HEADER = "X-Id"
    }
}
