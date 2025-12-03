package io.github.ackeecz.apythia.http.testing

import io.github.ackeecz.apythia.http.HttpApythia
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.shouldBe

public abstract class HttpApythiaTest<Sut : HttpApythia> : FunSpec() {

    private lateinit var underTest: Sut
    private lateinit var remoteDataSource: RemoteDataSource

    init {
        beforeEach {
            underTest = createSut().also { it.beforeEachTest() }
            remoteDataSource = createRemoteDataSource(underTest)
        }
        afterEach {
            underTest.afterEachTest()
        }

        arrangementTests()
    }

    protected abstract fun createSut(): Sut

    protected abstract fun createRemoteDataSource(sut: Sut): RemoteDataSource

    private fun FunSpec.arrangementTests() {
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
}
