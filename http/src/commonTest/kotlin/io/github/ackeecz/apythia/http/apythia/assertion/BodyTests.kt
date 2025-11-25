package io.github.ackeecz.apythia.http.apythia.assertion

import io.github.ackeecz.apythia.http.UnsupportedEncodingException
import io.github.ackeecz.apythia.http.apythia.HttpApythiaTest
import io.github.ackeecz.apythia.http.extension.HttpDslExtensionMock
import io.github.ackeecz.apythia.http.request.createActualRequest
import io.github.ackeecz.apythia.http.util.header.Headers
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.scopes.FunSpecContainerScope
import io.kotest.matchers.shouldBe

internal suspend fun FunSpecContainerScope.bodyTests(
    fixture: HttpApythiaTest.Fixture
) = with(fixture) {
    context("body") {
        emptyBodyTests(fixture)
        bytesBodyTests(fixture)
        plainTextBodyTests(fixture)
        multipartFormDataBodyTests(fixture)
        partialMultipartFormDataBodyTests(fixture)
        dslExtensionTests(fixture)
    }
}

private suspend fun FunSpecContainerScope.emptyBodyTests(
    fixture: HttpApythiaTest.Fixture
) = with(fixture) {
    context("empty") {
        test("failure") {
            underTest.actualBody = byteArrayOf(1, 2, 3)

            shouldFail {
                underTest.assertNextRequest {
                    body { empty() }
                }
            }
        }

        test("success") {
            underTest.actualBody = byteArrayOf()

            shouldNotFail {
                underTest.assertNextRequest {
                    body { empty() }
                }
            }
        }
    }
}

private suspend fun FunSpecContainerScope.bytesBodyTests(
    fixture: HttpApythiaTest.Fixture
) = with(fixture) {
    context("bytes") {
        test("failure") {
            val body = byteArrayOf(1, 2, 3)
            underTest.actualBody = body.reversed().toByteArray()

            shouldFail {
                underTest.assertNextRequest {
                    body { bytes(body) }
                }
            }
        }

        test("success") {
            val expected = byteArrayOf(1, 2, 3)
            underTest.actualBody = expected

            shouldNotFail {
                underTest.assertNextRequest {
                    body { bytes(expected) }
                }
            }
        }
    }
}

private suspend fun FunSpecContainerScope.plainTextBodyTests(
    fixture: HttpApythiaTest.Fixture
) = with(fixture) {
    context("plain text") {
        test("failure when body is different") {
            val body = "text"
            underTest.actualBody = body.reversed().encodeToByteArray()

            shouldFail {
                underTest.assertNextRequest {
                    body { plainText(body) }
                }
            }
        }

        test("failure on unsupported encoding in content type") {
            val expected = "text"
            underTest.actualBody = expected.encodeToByteArray()
            underTest.actualHeaders = mapOf(Headers.CONTENT_TYPE to listOf("text/plain; charset=utf-16"))

            shouldThrow<UnsupportedEncodingException> {
                underTest.assertNextRequest {
                    body { plainText(expected) }
                }
            }
        }

        test("success on supported UTF-8 encoding in content type") {
            val expected = "text"
            underTest.actualBody = expected.encodeToByteArray()
            underTest.actualHeaders = mapOf(Headers.CONTENT_TYPE to listOf("text/plain; charset=utf-8"))

            shouldNotFail {
                underTest.assertNextRequest {
                    body { plainText(expected) }
                }
            }
        }

        test("success when encoding is not specified in content type and supported encoding is used") {
            val expected = "text"
            underTest.actualBody = expected.encodeToByteArray()
            underTest.actualHeaders = mapOf(Headers.CONTENT_TYPE to listOf("text/plain"))

            shouldNotFail {
                underTest.assertNextRequest {
                    body { plainText(expected) }
                }
            }
        }

        test("success when content type header is missing and supported encoding is used") {
            val expected = "text"
            underTest.actualBody = expected.encodeToByteArray()
            underTest.actualHeaders = emptyMap()

            shouldNotFail {
                underTest.assertNextRequest {
                    body { plainText(expected) }
                }
            }
        }
    }
}

private suspend fun FunSpecContainerScope.dslExtensionTests(
    fixture: HttpApythiaTest.Fixture
) = with(fixture) {
    context("dsl extension") {
        test("failure") {
            val extension = HttpDslExtensionMock().also { it.failAssertion = true }

            shouldFail {
                underTest.assertNextRequest {
                    body { dslExtension(extension) }
                }
            }
        }

        test("success") {
            val extension = HttpDslExtensionMock().also { it.failAssertion = false }

            shouldNotFail {
                underTest.assertNextRequest {
                    body { dslExtension(extension) }
                }
            }
        }

        test("pass correct actual request to assertion") {
            val actualRequest = createActualRequest(
                method = "GET",
                url = "http://example.com",
                headers = mapOf(
                    "X-Custom-Header" to listOf("value"),
                    "Content-Type" to listOf("text/plain; charset=utf-8"),
                ),
                body = byteArrayOf(1, 2, 3),
            )
            underTest.actualRequest = actualRequest
            val expectedData = actualRequest.toTargetWithMessage()
            val extension = HttpDslExtensionMock()

            underTest.assertNextRequest {
                body { dslExtension(extension) }
            }

            extension.dataToAssert shouldBe expectedData
        }
    }
}
