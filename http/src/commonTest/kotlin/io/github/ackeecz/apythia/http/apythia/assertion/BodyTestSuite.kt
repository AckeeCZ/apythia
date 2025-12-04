package io.github.ackeecz.apythia.http.apythia.assertion

import io.github.ackeecz.apythia.http.apythia.HttpApythiaTest
import io.github.ackeecz.apythia.http.request.dsl.HttpRequestAssertion
import io.github.ackeecz.apythia.http.request.dsl.body.BodyAssertion
import io.github.ackeecz.apythia.http.util.header.Headers
import io.github.ackeecz.apythia.testing.http.shouldFail
import io.github.ackeecz.apythia.testing.http.shouldNotFail
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.core.spec.style.scopes.FunSpecContainerScope
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

internal suspend fun FunSpecContainerScope.bodyTestSuite(
    fixture: HttpApythiaTest.Fixture,
    arrangeHeaders: (Map<String, List<String>>) -> Unit,
    arrangeBody: (ByteArray) -> Unit,
    assertBody: suspend HttpRequestAssertion.(BodyAssertion.() -> Unit) -> Unit,
) = with(fixture) {
    context("body") {
        actualBodyTests(fixture, arrangeHeaders, arrangeBody, assertBody)
        emptyBodyTests(fixture, arrangeBody, assertBody)
        bytesBodyTests(fixture, arrangeBody, assertBody)
        plainTextBodyTests(fixture, arrangeHeaders, arrangeBody, assertBody)
    }
}

private suspend fun FunSpecContainerScope.actualBodyTests(
    fixture: HttpApythiaTest.Fixture,
    arrangeHeaders: (Map<String, List<String>>) -> Unit,
    arrangeBody: (ByteArray) -> Unit,
    assertBody: suspend HttpRequestAssertion.(BodyAssertion.() -> Unit) -> Unit,
) = with(fixture) {
    context("actual body") {
        test("get body data") {
            val expected = byteArrayOf(1, 2, 3)
            arrangeBody(expected)

            underTest.assertNextRequest {
                assertBody {
                    actualBody.data shouldBe expected
                }
            }
        }

        test("get null content type if missing") {
            arrangeHeaders(emptyMap())

            underTest.assertNextRequest {
                assertBody {
                    actualBody.contentType.shouldBeNull()
                }
            }
        }

        test("get content type if present") {
            val expected = "text/plain; charset=utf-8"
            arrangeHeaders(mapOf("Content-Type" to listOf(expected)))

            underTest.assertNextRequest {
                assertBody {
                    actualBody.contentType shouldBe expected
                }
            }
        }
    }
}

private suspend fun FunSpecContainerScope.emptyBodyTests(
    fixture: HttpApythiaTest.Fixture,
    arrangeBody: (ByteArray) -> Unit,
    assertBody: suspend HttpRequestAssertion.(BodyAssertion.() -> Unit) -> Unit,
) = with(fixture) {
    context("empty") {
        test("failure") {
            arrangeBody(byteArrayOf(1, 2, 3))

            shouldFail {
                underTest.assertNextRequest {
                    assertBody { empty() }
                }
            }
        }

        test("success") {
            arrangeBody(byteArrayOf())

            shouldNotFail {
                underTest.assertNextRequest {
                    assertBody { empty() }
                }
            }
        }
    }
}

private suspend fun FunSpecContainerScope.bytesBodyTests(
    fixture: HttpApythiaTest.Fixture,
    arrangeBody: (ByteArray) -> Unit,
    assertBody: suspend HttpRequestAssertion.(BodyAssertion.() -> Unit) -> Unit,
) = with(fixture) {
    context("bytes") {
        test("failure") {
            val body = byteArrayOf(1, 2, 3)
            arrangeBody(body.reversed().toByteArray())

            shouldFail {
                underTest.assertNextRequest {
                    assertBody { bytes(body) }
                }
            }
        }

        test("success") {
            val expected = byteArrayOf(1, 2, 3)
            arrangeBody(expected)

            shouldNotFail {
                underTest.assertNextRequest {
                    assertBody { bytes(expected) }
                }
            }
        }
    }
}

private suspend fun FunSpecContainerScope.plainTextBodyTests(
    fixture: HttpApythiaTest.Fixture,
    arrangeHeaders: (Map<String, List<String>>) -> Unit,
    arrangeBody: (ByteArray) -> Unit,
    assertBody: suspend HttpRequestAssertion.(BodyAssertion.() -> Unit) -> Unit,
) = with(fixture) {
    context("plainText (String)") {
        plainTextBodyTestSuite(
            fixture = fixture,
            createValueFrom = { it.toString() },
            callPlainText = { plainText(it) },
            arrangeBody = arrangeBody,
            arrangeHeaders = arrangeHeaders,
            assertBody = assertBody,
        )
    }
    context("plainText (Int)") {
        plainTextBodyTestSuite(
            fixture = fixture,
            createValueFrom = { it },
            callPlainText = { plainText(it) },
            arrangeBody = arrangeBody,
            arrangeHeaders = arrangeHeaders,
            assertBody = assertBody,
        )
    }
    context("plainText (Double)") {
        plainTextBodyTestSuite(
            fixture = fixture,
            createValueFrom = { it.toDouble() },
            callPlainText = { plainText(it) },
            arrangeBody = arrangeBody,
            arrangeHeaders = arrangeHeaders,
            assertBody = assertBody,
        )
    }
}

@Suppress("LongParameterList")
private suspend fun <T> FunSpecContainerScope.plainTextBodyTestSuite(
    fixture: HttpApythiaTest.Fixture,
    createValueFrom: (Int) -> T,
    callPlainText: BodyAssertion.(T) -> Unit,
    arrangeHeaders: (Map<String, List<String>>) -> Unit,
    arrangeBody: (ByteArray) -> Unit,
    assertBody: suspend HttpRequestAssertion.(BodyAssertion.() -> Unit) -> Unit,
) = with(fixture) {
    context("plain text") {
        test("failure when body is different") {
            val body = createValueFrom(1)
            arrangeBody(byteArrayOf(2, 3, 4))

            shouldFail {
                underTest.assertNextRequest {
                    assertBody { callPlainText(body) }
                }
            }
        }

        test("failure on unsupported encoding in content type") {
            val expected = createValueFrom(1)
            arrangeBody(expected.toString().encodeToByteArray())
            arrangeHeaders(mapOf(Headers.CONTENT_TYPE to listOf("text/plain; charset=utf-16")))

            shouldThrowAny {
                underTest.assertNextRequest {
                    assertBody { callPlainText(expected) }
                }
            }
        }

        test("success on supported UTF-8 encoding in content type") {
            val expected = createValueFrom(1)
            arrangeBody(expected.toString().encodeToByteArray())
            arrangeHeaders(mapOf(Headers.CONTENT_TYPE to listOf("text/plain; charset=utf-8")))

            shouldNotFail {
                underTest.assertNextRequest {
                    assertBody { callPlainText(expected) }
                }
            }
        }

        test("success when encoding is not specified in content type and supported encoding is used") {
            val expected = createValueFrom(1)
            arrangeBody(expected.toString().encodeToByteArray())
            arrangeHeaders(mapOf(Headers.CONTENT_TYPE to listOf("text/plain")))

            shouldNotFail {
                underTest.assertNextRequest {
                    assertBody { callPlainText(expected) }
                }
            }
        }

        test("success when content type header is missing and supported encoding is used") {
            val expected = createValueFrom(1)
            arrangeBody(expected.toString().encodeToByteArray())
            arrangeHeaders(emptyMap())

            shouldNotFail {
                underTest.assertNextRequest {
                    assertBody { callPlainText(expected) }
                }
            }
        }
    }
}
