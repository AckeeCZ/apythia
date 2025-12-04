package io.github.ackeecz.apythia.http.apythia.mocking

import io.github.ackeecz.apythia.http.apythia.HttpApythiaTest
import io.github.ackeecz.apythia.http.util.header.Headers
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.scopes.FunSpecContainerScope
import io.kotest.matchers.shouldBe

internal suspend fun FunSpecContainerScope.headersTests(
    fixture: HttpApythiaTest.Fixture
) = with(fixture) {
    context("headers") {
        callOnceTest { headers {} }

        test("default is empty") {
            underTest.mockNextResponse {}

            requireActualResponse().headers shouldBe emptyMap()
        }

        test("calling just headers sets empty headers") {
            underTest.mockNextResponse {
                headers {}
            }

            requireActualResponse().headers shouldBe emptyMap()
        }

        test("set content type header using body method and then set content type header explicitly") {
            shouldThrow<IllegalStateException> {
                underTest.mockNextResponse {
                    bytesBody(value = byteArrayOf(), contentType = jsonContentTypeValue)

                    headers {
                        header(Headers.CONTENT_TYPE, jsonContentTypeValue)
                    }
                }
            }
        }

        headerTests(fixture)
        headersWithSameNameTests(fixture)
        headersMapTests(fixture)
    }
}

private suspend fun FunSpecContainerScope.headerTests(
    fixture: HttpApythiaTest.Fixture
) = with(fixture) {
    context("header") {
        test("set header") {
            val expectedName = "Content-Type"
            val expectedValue = jsonContentTypeValue

            underTest.mockNextResponse {
                headers {
                    header(expectedName, expectedValue)
                }
            }

            requireActualResponse().headers shouldBe mapOf(expectedName to listOf(expectedValue))
        }

        test("set multiple headers of the same name") {
            val expectedName = "X-Custom-Header"
            val expectedValues = listOf("value1", "value2")

            underTest.mockNextResponse {
                headers {
                    expectedValues.forEach { value ->
                        header(expectedName, value)
                    }
                }
            }

            requireActualResponse().headers shouldBe mapOf(expectedName to expectedValues)
        }
    }
}

private suspend fun FunSpecContainerScope.headersWithSameNameTests(
    fixture: HttpApythiaTest.Fixture,
) = with(fixture) {
    context("headers") {
        test("set headers") {
            val expectedName = "X-Custom-Header"
            val expectedValues = listOf("value1", "value2")

            underTest.mockNextResponse {
                headers {
                    headers(expectedName, expectedValues)
                }
            }

            requireActualResponse().headers shouldBe mapOf(expectedName to expectedValues)
        }

        test("set headers of the same name multiple times") {
            val expectedName = "X-Custom-Header"
            val expectedValues1 = listOf("value1", "value2")
            val expectedValues2 = listOf("value3", "value4")
            val allExpectedValues = expectedValues1 + expectedValues2

            underTest.mockNextResponse {
                headers {
                    headers(expectedName, expectedValues1)
                    headers(expectedName, expectedValues2)
                }
            }

            requireActualResponse().headers shouldBe mapOf(expectedName to allExpectedValues)
        }
    }
}

private suspend fun FunSpecContainerScope.headersMapTests(
    fixture: HttpApythiaTest.Fixture,
) = with(fixture) {
    context("headers map") {
        test("set multiple headers") {
            val expectedHeaders = mapOf(
                "X-Custom-Header" to "value",
                "Content-Type" to jsonContentTypeValue,
            )

            underTest.mockNextResponse {
                headers {
                    headers(expectedHeaders)
                }
            }

            requireActualResponse().headers
                .mapValues { it.value.first() }
                .shouldBe(expectedHeaders)
        }
    }
}
