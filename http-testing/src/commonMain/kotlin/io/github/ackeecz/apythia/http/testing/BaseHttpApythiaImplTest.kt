package io.github.ackeecz.apythia.http.testing

import io.github.ackeecz.apythia.http.HttpApythia
import io.github.ackeecz.apythia.http.request.HttpMethod
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.shouldBe

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

        arrangementTests()
        actualRequestTests()
        multipartFormDataTests()
    }

    protected abstract fun createSut(): Sut

    protected abstract fun createRemoteDataSource(sut: Sut): RemoteDataSource

    private fun arrangementTests() {
        context("arrangement") {
            test("set status code") {
                val expectedCode = 305
                underTest.arrangeNextResponse {
                    statusCode(expectedCode)
                }

                val actual = remoteDataSource.getArrangedResponse()

                actual.statusCode shouldBe expectedCode
            }

            test("set empty headers") {
                underTest.arrangeNextResponse {}

                val actual = remoteDataSource.getArrangedResponse()

                actual.headers.shouldBeEmpty()
            }

            test("set headers") {
                val expected = mapOf(
                    "X-Custom-Header" to listOf("value"),
                    "X-Another-Header" to listOf("first-value", "second-value"),
                    "Content-Type" to listOf("application/json"),
                )
                underTest.arrangeNextResponse {
                    headers {
                        expected.forEach { (name, values) -> headers(name, values) }
                    }
                }

                val actual = remoteDataSource.getArrangedResponse()

                actual.headers.lowercaseKeys() shouldBe expected.lowercaseKeys()
            }

            test("set empty body") {
                underTest.arrangeNextResponse {}

                val actual = remoteDataSource.getArrangedResponse()

                actual.body.shouldBeEmpty()
            }

            test("set body") {
                val expected = byteArrayOf(1, 2, 3)
                underTest.arrangeNextResponse {
                    bytesBody(expected, contentType = null)
                }

                val actual = remoteDataSource.getArrangedResponse()

                actual.body shouldBe expected
            }

            test("arrange multiple responses") {
                val expectedCode = 305
                val expectedBody = byteArrayOf(1, 2, 3)
                underTest.arrangeNextResponse {
                    statusCode(expectedCode)
                }
                underTest.arrangeNextResponse {
                    bytesBody(expectedBody, contentType = null)
                }

                remoteDataSource.getArrangedResponse().statusCode shouldBe expectedCode
                remoteDataSource.getArrangedResponse().body shouldBe expectedBody
            }

            test("arrange 200 response with empty body") {
                underTest.arrangeNext200Response()
                val expectedCode = 200

                val actual = remoteDataSource.getArrangedResponse()

                actual.statusCode shouldBe expectedCode
                actual.body.shouldBeEmpty()
            }
        }
    }

    private fun actualRequestTests() {
        context("actual request") {
            test("POST method") {
                underTest.arrangeNext200Response()

                remoteDataSource.sendPostRequest()

                underTest.assertNextRequest {
                    method(HttpMethod.POST)
                }
            }

            test("URL") {
                underTest.arrangeNext200Response()
                val expectedUrl = "${remoteDataSource.baseUrl}/url/test"

                remoteDataSource.sendPostRequest(url = expectedUrl)

                underTest.assertNextRequest {
                    url { url(expectedUrl) }
                }
            }

            test("headers") {
                underTest.arrangeNext200Response()
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
                underTest.arrangeNext200Response()
                val expected = byteArrayOf(1, 2, 3)

                remoteDataSource.sendPostRequest(body = expected)

                underTest.assertNextRequest {
                    body { bytes(expected) }
                }
            }

            test("send multiple requests and then assert them") {
                underTest.arrangeNext200Response()
                underTest.arrangeNext200Response()
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
                underTest.arrangeNext200Response()
                underTest.arrangeNext200Response()
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
                underTest.arrangeNext200Response()
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
                underTest.arrangeNext200Response()
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
}
