package io.github.ackeecz.apythia.http.apythia.assertion

import io.github.ackeecz.apythia.http.apythia.HttpApythiaTest
import io.github.ackeecz.apythia.http.request.dsl.HttpRequestAssertion
import io.github.ackeecz.apythia.http.request.dsl.header.HeadersAssertion
import io.github.ackeecz.apythia.http.util.header.Headers
import io.github.ackeecz.apythia.testing.http.shouldFail
import io.github.ackeecz.apythia.testing.http.shouldNotFail
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.core.spec.style.scopes.FunSpecContainerScope
import io.kotest.matchers.maps.shouldContainAll

internal suspend fun FunSpecContainerScope.headersTestSuite(
    fixture: HttpApythiaTest.Fixture,
    arrangeHeaders: (Map<String, List<String>>) -> Unit,
    assertHeaders: suspend HttpRequestAssertion.(HeadersAssertion.() -> Unit) -> Unit,
) = with(fixture) {
    context("headers") {
        headerTests(fixture, arrangeHeaders, assertHeaders)
        headersBlockTests(fixture, arrangeHeaders, assertHeaders)
        contentTypeTests(fixture, arrangeHeaders, assertHeaders)

        test("success when actual header name is in a different case") {
            val expectedHeader = "nAmE" to listOf("value")
            arrangeHeaders(mapOf(expectedHeader))

            shouldNotFail {
                underTest.assertNextRequest {
                    assertHeaders {
                        headers(expectedHeader.first.lowercase(), expectedHeader.second)
                    }
                }
            }
        }

        test("get actual headers") {
            val expected = mapOf(
                "Accept-Encoding" to listOf("gzip", "deflate"),
                "cOnTeNT-TYPe" to listOf("application/json"),
            )
            arrangeHeaders(expected)

            underTest.assertNextRequest {
                assertHeaders {
                    actualHeaders shouldContainAll expected
                }
            }
        }

        test("block can be called only once") {
            arrangeHeaders(mapOf())
            underTest.assertNextRequest {
                assertHeaders {}

                shouldThrow<IllegalStateException> {
                    assertHeaders {}
                }
            }
        }
    }
}

private suspend fun FunSpecContainerScope.headerTests(
    fixture: HttpApythiaTest.Fixture,
    arrangeHeaders: (Map<String, List<String>>) -> Unit,
    assertHeaders: suspend HttpRequestAssertion.(HeadersAssertion.() -> Unit) -> Unit,
) = with(fixture) {
    context("header") {
        test("failure when missing") {
            arrangeHeaders(mapOf("key" to listOf("value")))

            shouldFail {
                underTest.assertNextRequest {
                    assertHeaders { header(name = "missing-key", value = "value") }
                }
            }
        }

        test("failure when incorrect value") {
            val key = "key"
            arrangeHeaders(mapOf(key to listOf("value")))

            shouldFail {
                underTest.assertNextRequest {
                    assertHeaders { header(key, "another-value") }
                }
            }
        }

        test("success with single header") {
            val expectedHeader = "key" to listOf("value")
            arrangeHeaders(mapOf(expectedHeader))

            shouldNotFail {
                underTest.assertNextRequest {
                    assertHeaders {
                        header(
                            name = expectedHeader.first,
                            value = expectedHeader.second.first(),
                        )
                    }
                }
            }
        }

        test("success when there are multiple headers with the same name and at least one value matches") {
            val expectedKey = "key"
            val expectedValues = listOf("value1", "value2")
            arrangeHeaders(mapOf(expectedKey to expectedValues.reversed()))

            shouldNotFail {
                underTest.assertNextRequest {
                    assertHeaders {
                        header(expectedKey, expectedValues.first())
                    }
                }
            }
        }
    }
}

private suspend fun FunSpecContainerScope.headersBlockTests(
    fixture: HttpApythiaTest.Fixture,
    arrangeHeaders: (Map<String, List<String>>) -> Unit,
    assertHeaders: suspend HttpRequestAssertion.(HeadersAssertion.() -> Unit) -> Unit,
) = with(fixture) {
    context("headers") {
        test("failure on empty list") {
            arrangeHeaders(emptyMap())
            shouldThrowAny {
                underTest.assertNextRequest {
                    assertHeaders { headers("key", emptyList()) }
                }
            }
        }

        test("failure when missing value") {
            val key = "key"
            val expectedValues = listOf("value1", "value2")
            arrangeHeaders(mapOf(key to listOf(expectedValues.first())))

            shouldFail {
                underTest.assertNextRequest {
                    assertHeaders { headers(key, expectedValues) }
                }
            }
        }

        test("failure when incorrect value") {
            val key = "key"
            val expectedValues = listOf("value1", "value2")
            arrangeHeaders(mapOf(key to listOf(expectedValues.first(), "incorrect-value")))

            shouldFail {
                underTest.assertNextRequest {
                    assertHeaders { headers(key, expectedValues) }
                }
            }
        }

        test("succeed when all values match") {
            val key = "key"
            val expectedValues = listOf("value1", "value2")
            arrangeHeaders(mapOf(key to expectedValues))

            shouldNotFail {
                underTest.assertNextRequest {
                    assertHeaders { headers(key, expectedValues) }
                }
            }
        }

        test("succeed when all values match and there are extra values") {
            val key = "key"
            val expectedValues = listOf("value1", "value2")
            arrangeHeaders(mapOf(key to expectedValues + listOf("extra-value")))

            shouldNotFail {
                underTest.assertNextRequest {
                    assertHeaders { headers(key, expectedValues) }
                }
            }
        }
    }
}

private suspend fun FunSpecContainerScope.contentTypeTests(
    fixture: HttpApythiaTest.Fixture,
    arrangeHeaders: (Map<String, List<String>>) -> Unit,
    assertHeaders: suspend HttpRequestAssertion.(HeadersAssertion.() -> Unit) -> Unit,
) = with(fixture) {
    context("contentType") {
        test("failure when incorrect mime type") {
            arrangeHeaders(mapOf(Headers.CONTENT_TYPE to listOf("text/plain")))

            shouldFail {
                underTest.assertNextRequest {
                    assertHeaders { contentType(mimeType = "application/json") }
                }
            }
        }

        test("failure when incorrect parameters") {
            val mimeType = "application/json"
            arrangeHeaders(mapOf(Headers.CONTENT_TYPE to listOf("$mimeType; charset=utf-8")))

            shouldFail {
                underTest.assertNextRequest {
                    assertHeaders { contentType(mimeType, mapOf("charset" to "utf-16")) }
                }
            }
        }

        test("success when correct mime type and parameters") {
            val mimeType = "application/json"
            val paramKey = "charset"
            val paramValue = "utf-8"
            val parameters = mapOf(paramKey to paramValue)
            arrangeHeaders(mapOf(Headers.CONTENT_TYPE to listOf("$mimeType; $paramKey=$paramValue")))

            shouldNotFail {
                underTest.assertNextRequest {
                    assertHeaders { contentType(mimeType, parameters) }
                }
            }
        }
    }
}
