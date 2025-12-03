package io.github.ackeecz.apythia.http.apythia.assertion

import io.github.ackeecz.apythia.http.apythia.HttpApythiaTest
import io.github.ackeecz.apythia.http.util.header.Headers
import io.kotest.core.spec.style.scopes.FunSpecContainerScope

internal suspend fun FunSpecContainerScope.rootHeadersTests(
    fixture: HttpApythiaTest.Fixture
) = with(fixture) {
    context("headers") {
        headerTests(fixture)
        headersTests(fixture)
        contentTypeTests(fixture)

        test("success when actual header name is in a different case") {
            val expectedHeader = "nAmE" to listOf("value")
            underTest.actualHeaders = mapOf(expectedHeader)

            shouldNotFail {
                underTest.assertNextRequest {
                    headers {
                        headers(expectedHeader.first.lowercase(), expectedHeader.second)
                    }
                }
            }
        }
    }
}

private suspend fun FunSpecContainerScope.headerTests(
    fixture: HttpApythiaTest.Fixture
) = with(fixture) {
    context("header") {
        test("failure when incorrect value") {
            val key = "key"
            underTest.actualHeaders = mapOf(key to listOf("value"))

            shouldFail {
                underTest.assertNextRequest {
                    headers { header(key, "another-value") }
                }
            }
        }

        test("failure when missing") {
            underTest.actualHeaders = mapOf("key" to listOf("value"))

            shouldFail {
                underTest.assertNextRequest {
                    headers { header(name = "missing-key", value = "value") }
                }
            }
        }

        test("success with single header") {
            val expectedHeader = "key" to listOf("value")
            underTest.actualHeaders = mapOf(expectedHeader)

            shouldNotFail {
                underTest.assertNextRequest {
                    headers {
                        header(
                            name = expectedHeader.first,
                            value = expectedHeader.second.first(),
                        )
                    }
                }
            }
        }

        test("success with multiple headers with the same name and different order of values") {
            val expectedKey = "key"
            val expectedValues = listOf("value1", "value2")
            underTest.actualHeaders = mapOf(expectedKey to expectedValues.reversed())

            shouldNotFail {
                underTest.assertNextRequest {
                    headers {
                        expectedValues.forEach {
                            header(expectedKey, it)
                        }
                    }
                }
            }
        }
    }
}

private suspend fun FunSpecContainerScope.headersTests(
    fixture: HttpApythiaTest.Fixture
) = with(fixture) {
    context("headers") {
        test("failure when incorrect value") {
            val key = "key"
            val expectedValues = listOf("value1", "value2")
            underTest.actualHeaders = mapOf(key to listOf(expectedValues.first(), "incorrect-value"))

            shouldFail {
                underTest.assertNextRequest {
                    headers { headers(key, expectedValues) }
                }
            }
        }

        test("failure when missing value") {
            val key = "key"
            val expectedValues = listOf("value1", "value2")
            underTest.actualHeaders = mapOf(key to listOf(expectedValues.first()))

            shouldFail {
                underTest.assertNextRequest {
                    headers { headers(key, expectedValues) }
                }
            }
        }

        test("failure when extra value") {
            val key = "key"
            val expectedValues = listOf("value1", "value2")
            underTest.actualHeaders = mapOf(key to expectedValues + listOf("extra-value"))

            shouldFail {
                underTest.assertNextRequest {
                    headers { headers(key, expectedValues) }
                }
            }
        }

        test("succeed") {
            val key = "key"
            val expectedValues = listOf("value1", "value2")
            underTest.actualHeaders = mapOf(key to expectedValues)

            shouldNotFail {
                underTest.assertNextRequest {
                    headers { headers(key, expectedValues) }
                }
            }
        }
    }
}

private suspend fun FunSpecContainerScope.contentTypeTests(
    fixture: HttpApythiaTest.Fixture
) = with(fixture) {
    context("contentType") {
        test("failure when incorrect mime type") {
            underTest.actualHeaders = mapOf(Headers.CONTENT_TYPE to listOf("text/plain"))

            shouldFail {
                underTest.assertNextRequest {
                    headers { contentType(mimeType = "application/json") }
                }
            }
        }

        test("failure when incorrect parameters") {
            val mimeType = "application/json"
            underTest.actualHeaders = mapOf(Headers.CONTENT_TYPE to listOf("$mimeType; charset=utf-8"))

            shouldFail {
                underTest.assertNextRequest {
                    headers { contentType(mimeType, mapOf("charset" to "utf-16")) }
                }
            }
        }

        test("success when correct mime type and parameters") {
            val mimeType = "application/json"
            val paramKey = "charset"
            val paramValue = "utf-8"
            val parameters = mapOf(paramKey to paramValue)
            underTest.actualHeaders = mapOf(Headers.CONTENT_TYPE to listOf("$mimeType; $paramKey=$paramValue"))

            shouldNotFail {
                underTest.assertNextRequest {
                    headers { contentType(mimeType, parameters) }
                }
            }
        }
    }
}
