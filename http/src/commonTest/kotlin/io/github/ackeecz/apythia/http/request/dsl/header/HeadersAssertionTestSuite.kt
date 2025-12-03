@file:Suppress("MatchingDeclarationName")

package io.github.ackeecz.apythia.http.request.dsl.header

import io.github.ackeecz.apythia.http.request.dsl.HttpRequestAssertion
import io.github.ackeecz.apythia.http.request.dsl.HttpRequestAssertionImpl
import io.github.ackeecz.apythia.http.request.header.ExpectedHeaders
import io.github.ackeecz.apythia.http.util.header.lowercaseKeys
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

internal abstract class HeadersAssertionFixture {

    lateinit var underTest: HttpRequestAssertionImpl

    abstract val HttpRequestAssertionImpl.expectedHeaders: ExpectedHeaders

    val HttpRequestAssertionImpl.headers
        get() = expectedHeaders.headers

    fun beforeEach() {
        underTest = HttpRequestAssertionImpl()
    }

    abstract fun HttpRequestAssertion.headersTest(test: HeadersAssertion.() -> Unit)
}

internal fun FunSpec.headersAssertionTestSuite(fixture: HeadersAssertionFixture) = with(fixture) {
    test("headers not asserted when just empty headers assertion block is called") {
        underTest.headersTest {}

        underTest.headers.shouldBeNull()
    }

    headerTests(fixture)
    headersTests(fixture)
    contentTypeTests(fixture)
}

private fun FunSpec.headerTests(fixture: HeadersAssertionFixture) = with(fixture) {
    context("header") {
        test("set single value") {
            val name = "name"
            val value = "value"
            val expected = mapOf(name to listOf(value))

            underTest.headersTest {
                header(name, value)
            }

            underTest.headers shouldBe expected
        }

        test("set multiple values for single header") {
            val name = "name"
            val value1 = "value1"
            val value2 = "value2"
            val expected = mapOf(name to listOf(value1, value2))

            underTest.headersTest {
                header(name, value1)
                header(name, value2)
            }

            underTest.headers shouldBe expected
        }

        test("set multiple headers with different names") {
            val name1 = "name1"
            val name2 = "name2"
            val value1 = "value1"
            val value2 = "value2"
            val expected = mapOf(
                name1 to listOf(value1),
                name2 to listOf(value2),
            )

            underTest.headersTest {
                header(name1, value1)
                header(name2, value2)
            }

            underTest.headers shouldBe expected
        }
    }
}

private fun FunSpec.headersTests(fixture: HeadersAssertionFixture) = with(fixture) {
    context("headers") {
        test("set single header values") {
            val name = "name"
            val values = listOf("value1", "value2")
            val expected = mapOf(name to values)

            underTest.headersTest {
                headers(name, values)
            }

            underTest.headers shouldBe expected
        }

        test("set multiple header values") {
            val name1 = "name1"
            val name2 = "name2"
            val values = listOf("value1", "value2")
            val expected = mapOf(
                name1 to values,
                name2 to values,
            )

            underTest.headersTest {
                headers(name1, values)
                headers(name2, values)
            }

            underTest.headers shouldBe expected
        }

        test("set multiple values for single header") {
            val name = "name"
            val values1 = listOf("value1", "value2")
            val values2 = listOf("value3", "value4")
            val expected = mapOf(name to values1 + values2)

            underTest.headersTest {
                headers(name, values1)
                headers(name, values2)
            }

            underTest.headers shouldBe expected
        }

        test("fail on empty list") {
            shouldThrow<IllegalArgumentException> {
                underTest.headersTest {
                    headers("name", emptyList())
                }
            }
        }
    }
}

private fun FunSpec.contentTypeTests(fixture: HeadersAssertionFixture) = with(fixture) {
    context("contentType") {
        test("set without parameters") {
            val mimeType = "application/json"
            val expected = mapOf("content-type" to listOf(mimeType))

            underTest.headersTest {
                contentType(mimeType, emptyMap())
            }

            underTest.headers?.lowercaseKeys() shouldBe expected
        }

        test("parameters are empty by default") {
            val expected = "application/json"

            underTest.headersTest {
                contentType(expected)
            }

            underTest.headers?.values?.first()?.first() shouldBe expected
        }

        test("set with parameters") {
            val mimeType = "application/json"
            val parameter1Name = "charset"
            val parameter1Value = "utf-8"
            val parameter2Name = "another-param"
            val parameter2Value = "value"
            val parameters = mapOf(parameter1Name to parameter1Value, parameter2Name to parameter2Value)
            val expected = "$mimeType; $parameter1Name=$parameter1Value; $parameter2Name=$parameter2Value"

            underTest.headersTest {
                contentType(mimeType, parameters)
            }

            underTest.headers?.values?.first()?.first() shouldBe expected
        }

        test("fail when called more than once") {
            underTest.headersTest {
                contentType("application/json")

                shouldThrow<IllegalStateException> {
                    contentType("text/plain")
                }
            }
        }
    }
}
