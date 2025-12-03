package io.github.ackeecz.apythia.http.response.dsl

import io.github.ackeecz.apythia.http.extension.DslExtensionConfigProviderImpl
import io.github.ackeecz.apythia.http.response.dsl.HttpResponseArrangementTest.Fixture.Companion.JsonContentTypeValue
import io.github.ackeecz.apythia.http.util.header.Headers
import io.github.ackeecz.apythia.testing.http.callOnceTest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

internal class HttpResponseArrangementTest : FunSpec({

    val testFixture = Fixture()

    beforeEach {
        testFixture.beforeEach()
    }

    statusCodeTests(testFixture)
    headersTests(testFixture)
    bodyTests(testFixture)

    test("set all values at once") {
        val underTest = testFixture.underTest
        val expectedStatusCode = 404
        val expectedHeaders = mapOf("X-Custom-Header" to listOf("value"))
        val expectedBody = byteArrayOf(1, 2, 3)

        underTest.statusCode(expectedStatusCode)
        underTest.headers {
            expectedHeaders.forEach { (name, values) -> headers(name, values) }
        }
        underTest.bytesBody(value = expectedBody, contentType = null)

        underTest.httpResponse.statusCode shouldBe expectedStatusCode
        underTest.httpResponse.headers shouldBe expectedHeaders
        underTest.httpResponse.body shouldBe expectedBody
    }
}) {

    class Fixture {

        lateinit var underTest: HttpResponseArrangementImpl

        fun beforeEach() {
            underTest = HttpResponseArrangementImpl(DslExtensionConfigProviderImpl(emptyList()))
        }

        companion object {

            @Suppress("ConstPropertyName")
            const val JsonContentTypeValue = "application/json"
        }
    }
}

private fun FunSpec.statusCodeTests(fixture: HttpResponseArrangementTest.Fixture) = with(fixture) {
    context("status code") {
        test("default is 200") {
            underTest.httpResponse.statusCode shouldBe 200
        }

        test("set") {
            val expectedStatusCode = 404

            underTest.statusCode(expectedStatusCode)

            underTest.httpResponse.statusCode shouldBe expectedStatusCode
        }

        callOnceTest { underTest.statusCode(200) }
    }
}

private fun FunSpec.headersTests(fixture: HttpResponseArrangementTest.Fixture) = with(fixture) {
    context("headers") {
        test("default is empty") {
            underTest.httpResponse.headers shouldBe emptyMap()
        }

        test("calling just headers sets empty headers") {
            underTest.headers {}

            underTest.httpResponse.headers shouldBe emptyMap()
        }

        callOnceTest { underTest.headers {} }

        test("set header") {
            val expectedName = "Content-Type"
            val expectedValue = JsonContentTypeValue

            underTest.headers {
                header(expectedName, expectedValue)
            }

            underTest.httpResponse.headers shouldBe mapOf(expectedName to listOf(expectedValue))
        }

        test("set multiple headers of the same name") {
            val expectedName = "X-Custom-Header"
            val expectedValues = listOf("value1", "value2")

            underTest.headers {
                expectedValues.forEach { value ->
                    header(expectedName, value)
                }
            }

            underTest.httpResponse.headers shouldBe mapOf(expectedName to expectedValues)
        }

        test("set headers") {
            val expectedName = "X-Custom-Header"
            val expectedValues = listOf("value1", "value2")

            underTest.headers {
                headers(expectedName, expectedValues)
            }

            underTest.httpResponse.headers shouldBe mapOf(expectedName to expectedValues)
        }

        test("set headers of the same name multiple times") {
            val expectedName = "X-Custom-Header"
            val expectedValues1 = listOf("value1", "value2")
            val expectedValues2 = listOf("value3", "value4")
            val allExpectedValues = expectedValues1 + expectedValues2

            underTest.headers {
                headers(expectedName, expectedValues1)
                headers(expectedName, expectedValues2)
            }

            underTest.httpResponse.headers shouldBe mapOf(expectedName to allExpectedValues)
        }

        test("set content type header using body method and then set content type header explicitly") {
            underTest.bytesBody(value = byteArrayOf(), contentType = JsonContentTypeValue)

            shouldThrow<IllegalStateException> {
                underTest.headers {
                    header(Headers.CONTENT_TYPE, JsonContentTypeValue)
                }
            }
        }
    }
}
