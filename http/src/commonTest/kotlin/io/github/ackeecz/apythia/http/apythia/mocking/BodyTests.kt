package io.github.ackeecz.apythia.http.apythia.mocking

import io.github.ackeecz.apythia.http.Charset
import io.github.ackeecz.apythia.http.apythia.HttpApythiaTest
import io.github.ackeecz.apythia.http.response.dsl.HttpResponseMockBuilder
import io.github.ackeecz.apythia.http.util.header.Headers
import io.github.ackeecz.apythia.http.util.header.contentType
import io.github.ackeecz.apythia.http.util.header.randomCase
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.scopes.FunSpecContainerScope
import io.kotest.matchers.maps.shouldHaveSize
import io.kotest.matchers.shouldBe

internal suspend fun FunSpecContainerScope.bodyTests(
    fixture: HttpApythiaTest.Fixture,
) = with(fixture) {
    context("body") {
        test("default value is empty") {
            underTest.mockNextResponse {}

            requireActualResponse().body shouldBe byteArrayOf()
        }

        test("content type is null by default") {
            underTest.mockNextResponse {}

            requireActualResponse().headers.contentType shouldBe null
        }

        test("body can be set just once no matter the content type method") {
            underTest.mockNextResponse {
                bytesBody(value = byteArrayOf(), contentType = null)

                shouldThrow<IllegalStateException> {
                    plainTextBody(value = "")
                }
            }
        }

        bytesBodyTests(fixture)
        plainTextBodyTests(fixture)
    }
}

private suspend fun FunSpecContainerScope.bytesBodyTests(
    fixture: HttpApythiaTest.Fixture,
) = with(fixture) {
    context("bytes") {
        callOnceTest { bytesBody(value = byteArrayOf(), contentType = null) }

        test("set value") {
            val expectedBody = byteArrayOf(1, 2, 3)

            underTest.mockNextResponse {
                bytesBody(value = expectedBody, contentType = null)
            }

            requireActualResponse().body shouldBe expectedBody
        }

        test("set content type") {
            val expectedContentType = jsonContentTypeValue

            underTest.mockNextResponse {
                bytesBody(value = byteArrayOf(), contentType = expectedContentType)
            }

            requireActualResponse().headers.contentType shouldBe expectedContentType
        }

        test("do not set content type when it is null") {
            underTest.mockNextResponse {
                bytesBody(value = byteArrayOf(), contentType = null)
            }

            requireActualResponse().headers.contentType shouldBe null
        }

        test("preserve other headers when setting content type") {
            val expectedContentType = jsonContentTypeValue
            val expectedOtherHeader = "X-Custom-Header"
            val expectedOtherHeaderValue = "value"

            underTest.mockNextResponse {
                headers {
                    header(expectedOtherHeader, expectedOtherHeaderValue)
                }
                bytesBody(value = byteArrayOf(), contentType = expectedContentType)
            }

            with(requireActualResponse().headers) {
                contentType shouldBe expectedContentType
                get(expectedOtherHeader) shouldBe listOf(expectedOtherHeaderValue)
            }
        }

        test("preserve other headers when not setting content type") {
            val expectedOtherHeader = "X-Custom-Header"
            val expectedOtherHeaderValue = "value"

            underTest.mockNextResponse {
                headers {
                    header(expectedOtherHeader, expectedOtherHeaderValue)
                }
                bytesBody(value = byteArrayOf(), contentType = null)
            }

            requireActualResponse().headers[expectedOtherHeader] shouldBe listOf(expectedOtherHeaderValue)
        }

        checkContentTypeHeaderPresenceWhenBodySetTest(
            fixture = fixture,
            setBodyWithContentType = {
                bytesBody(value = byteArrayOf(), contentType = "text/plain")
            }
        )
    }
}

private suspend fun FunSpecContainerScope.plainTextBodyTests(
    fixture: HttpApythiaTest.Fixture,
) = with(fixture) {
    context("plain text") {
        callOnceTest { plainTextBody(value = "") }

        test("set value with default UTF-8 charset") {
            val expectedValue = "Hello, world!"
            underTest.mockNextResponse { plainTextBody(value = expectedValue) }

            requireActualResponse().body.decodeToString() shouldBe expectedValue
        }

        Charset.entries.forEach { charset ->
            test("set value with $charset charset") {
                val expectedValue = "Hello, world!"
                underTest.mockNextResponse {
                    plainTextBody(value = expectedValue, charset = charset)
                }

                val actualValue = requireActualResponse().body

                val decodedActualValue = when (charset) {
                    Charset.UTF_8 -> actualValue.decodeToString()
                }
                decodedActualValue shouldBe expectedValue
            }
        }

        Charset.entries.forEach { charset ->
            test("set content type to text/plain with ${charset.name} charset") {
                val expectedContentType = "text/plain; charset=${charset.name}"

                underTest.mockNextResponse {
                    plainTextBody(value = "", charset = charset)
                }

                requireActualResponse().headers.contentType shouldBe expectedContentType
            }
        }

        test("preserve other headers when content type is set") {
            val expectedOtherHeader = "X-Custom-Header"
            val expectedOtherHeaderValue = "value"

            underTest.mockNextResponse {
                headers {
                    header(expectedOtherHeader, expectedOtherHeaderValue)
                }
                plainTextBody(value = "")
            }

            val headers = requireActualResponse().headers
            headers.shouldHaveSize(2)
            headers[expectedOtherHeader] shouldBe listOf(expectedOtherHeaderValue)
        }

        checkContentTypeHeaderPresenceWhenBodySetTest(
            fixture = fixture,
            setBodyWithContentType = { plainTextBody(value = "") },
        )
    }
}

private suspend fun FunSpecContainerScope.checkContentTypeHeaderPresenceWhenBodySetTest(
    fixture: HttpApythiaTest.Fixture,
    setBodyWithContentType: HttpResponseMockBuilder.() -> Unit,
) = with(fixture) {
    test("throw exception when content type header already present") {
        underTest.mockNextResponse {
            headers {
                header(Headers.CONTENT_TYPE.randomCase(), jsonContentTypeValue)
            }

            shouldThrow<IllegalStateException> {
                setBodyWithContentType()
            }
        }
    }
}
