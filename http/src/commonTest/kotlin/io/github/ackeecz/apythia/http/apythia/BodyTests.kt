package io.github.ackeecz.apythia.http.apythia

import io.github.ackeecz.apythia.http.UnsupportedEncodingException
import io.github.ackeecz.apythia.http.util.header.Headers
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.spec.style.scopes.FunSpecContainerScope

internal fun FunSpec.bodyTests(fixture: HttpApythiaTest.Fixture) = with(fixture) {
    context("body") {
        emptyBodyTests(fixture)
        bytesBodyTests(fixture)
        plainTextBodyTests(fixture)
        multipartFormDataBodyTests(fixture)
        partialMultipartFormDataBodyTests(fixture)
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
