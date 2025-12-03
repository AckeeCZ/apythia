package io.github.ackeecz.apythia.http.response.dsl

import io.github.ackeecz.apythia.http.Charset
import io.github.ackeecz.apythia.http.callOnceTest
import io.github.ackeecz.apythia.http.response.dsl.HttpResponseArrangementTest.Fixture.Companion.JsonContentTypeValue
import io.github.ackeecz.apythia.http.util.header.Headers
import io.github.ackeecz.apythia.http.util.header.contentType
import io.github.ackeecz.apythia.http.util.header.randomCase
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.spec.style.scopes.FunSpecContainerScope
import io.kotest.matchers.maps.shouldHaveSize
import io.kotest.matchers.shouldBe

internal fun FunSpec.bodyTests(fixture: HttpResponseArrangementTest.Fixture) = with(fixture) {
    context("body") {
        test("default value is empty") {
            underTest.httpResponse.body shouldBe byteArrayOf()
        }

        test("content type is null by default") {
            underTest.httpResponse.headers.contentType shouldBe null
        }

        test("body can be set just once no matter the content type method") {
            underTest.bytesBody(value = byteArrayOf(), contentType = null)

            shouldThrow<IllegalStateException> {
                underTest.plainTextBody(value = "")
            }
        }

        bytesBodyTests(fixture)
        plainTextBodyTests(fixture)
    }
}

private suspend fun FunSpecContainerScope.bytesBodyTests(
    fixture: HttpResponseArrangementTest.Fixture
) = with(fixture) {
    context("bytes") {
        callOnceTest { underTest.bytesBody(value = byteArrayOf(), contentType = null) }

        test("set value") {
            val expectedBody = byteArrayOf(1, 2, 3)

            underTest.bytesBody(value = expectedBody, contentType = null)

            underTest.httpResponse.body shouldBe expectedBody
        }

        test("set content type") {
            val expectedContentType = JsonContentTypeValue

            underTest.bytesBody(value = byteArrayOf(), contentType = expectedContentType)

            underTest.httpResponse.headers.contentType shouldBe expectedContentType
        }

        test("do not set content type when it is null") {
            underTest.bytesBody(value = byteArrayOf(), contentType = null)

            underTest.httpResponse.headers.contentType shouldBe null
        }

        test("preserve other headers when setting content type") {
            val expectedContentType = JsonContentTypeValue
            val expectedOtherHeader = "X-Custom-Header"
            val expectedOtherHeaderValue = "value"
            underTest.headers {
                header(expectedOtherHeader, expectedOtherHeaderValue)
            }

            underTest.bytesBody(value = byteArrayOf(), contentType = expectedContentType)

            underTest.httpResponse.headers.contentType shouldBe expectedContentType
            underTest.httpResponse.headers[expectedOtherHeader] shouldBe listOf(expectedOtherHeaderValue)
        }

        test("preserve other headers when not setting content type") {
            val expectedOtherHeader = "X-Custom-Header"
            val expectedOtherHeaderValue = "value"
            underTest.headers {
                header(expectedOtherHeader, expectedOtherHeaderValue)
            }

            underTest.bytesBody(value = byteArrayOf(), contentType = null)

            underTest.httpResponse.headers[expectedOtherHeader] shouldBe listOf(expectedOtherHeaderValue)
        }

        checkContentTypeHeaderPresenceWhenBodySetTest(
            fixture = fixture,
            setBodyWithContentType = {
                underTest.bytesBody(value = byteArrayOf(), contentType = "text/plain")
            }
        )
    }
}

private suspend fun FunSpecContainerScope.plainTextBodyTests(
    fixture: HttpResponseArrangementTest.Fixture
) = with(fixture) {
    context("plain text") {
        callOnceTest { underTest.plainTextBody(value = "") }

        test("set value with default UTF-8 charset") {
            val expectedValue = "Hello, world!"
            underTest.plainTextBody(value = expectedValue)

            underTest.httpResponse.body.decodeToString() shouldBe expectedValue
        }

        Charset.entries.forEach { charset ->
            test("set value with $charset charset") {
                val expectedValue = "Hello, world!"
                underTest.plainTextBody(value = expectedValue, charset = charset)

                val actualValue = underTest.httpResponse.body

                val decodedActualValue = when (charset) {
                    Charset.UTF_8 -> actualValue.decodeToString()
                }
                decodedActualValue shouldBe expectedValue
            }
        }

        Charset.entries.forEach { charset ->
            test("set content type to text/plain with ${charset.name} charset") {
                val expectedContentType = "text/plain; charset=${charset.name}"

                underTest.plainTextBody(value = "", charset = charset)

                underTest.httpResponse.headers.contentType shouldBe expectedContentType
            }
        }

        test("preserve other headers when content type is set") {
            val expectedOtherHeader = "X-Custom-Header"
            val expectedOtherHeaderValue = "value"
            underTest.headers {
                header(expectedOtherHeader, expectedOtherHeaderValue)
            }

            underTest.plainTextBody(value = "")

            val headers = underTest.httpResponse.headers
            headers.shouldHaveSize(2)
            headers[expectedOtherHeader] shouldBe listOf(expectedOtherHeaderValue)
        }

        checkContentTypeHeaderPresenceWhenBodySetTest(
            fixture = fixture,
            setBodyWithContentType = { underTest.plainTextBody(value = "") },
        )
    }
}

private suspend fun FunSpecContainerScope.checkContentTypeHeaderPresenceWhenBodySetTest(
    fixture: HttpResponseArrangementTest.Fixture,
    setBodyWithContentType: () -> Unit,
) = with(fixture) {
    test("throw exception when content type header already present") {
        underTest.headers {
            header(Headers.CONTENT_TYPE.randomCase(), JsonContentTypeValue)
        }

        shouldThrow<IllegalStateException> {
            setBodyWithContentType()
        }
    }
}
