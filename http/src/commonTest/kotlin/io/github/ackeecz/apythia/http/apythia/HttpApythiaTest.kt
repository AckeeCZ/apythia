package io.github.ackeecz.apythia.http.apythia

import io.github.ackeecz.apythia.http.HttpApythia
import io.github.ackeecz.apythia.http.apythia.assertion.methodTests
import io.github.ackeecz.apythia.http.apythia.assertion.queryTests
import io.github.ackeecz.apythia.http.apythia.assertion.requestBodyTests
import io.github.ackeecz.apythia.http.apythia.assertion.requestHeadersTests
import io.github.ackeecz.apythia.http.apythia.assertion.urlTests
import io.github.ackeecz.apythia.http.apythia.mocking.bodyTests
import io.github.ackeecz.apythia.http.apythia.mocking.headersTests
import io.github.ackeecz.apythia.http.apythia.mocking.statusCodeTests
import io.github.ackeecz.apythia.http.extension.DslExtensionConfig
import io.github.ackeecz.apythia.http.extension.DslExtensionConfigMock
import io.github.ackeecz.apythia.http.extension.DslExtensionConfigProvider
import io.github.ackeecz.apythia.http.extension.getDslExtensionConfig
import io.github.ackeecz.apythia.http.request.dsl.body.BodyAssertion
import io.github.ackeecz.apythia.http.request.dsl.header.HeadersAssertion
import io.github.ackeecz.apythia.http.request.dsl.url.QueryAssertion
import io.github.ackeecz.apythia.http.request.dsl.url.UrlAssertion
import io.github.ackeecz.apythia.http.response.HttpResponse
import io.github.ackeecz.apythia.http.response.dsl.HttpResponseMockBuilder
import io.github.ackeecz.apythia.testing.http.HttpApythiaMock
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.spec.style.scopes.FunSpecContainerScope
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.random.Random

internal class HttpApythiaTest : FunSpec({

    val fixture = Fixture()

    beforeEach {
        fixture.beforeEach()
    }

    dslExtensionConfigTests(fixture)
    assertionTests(fixture)
    mockingTests(fixture)
}) {

    class Fixture {

        val jsonContentTypeValue = "application/json"

        lateinit var underTest: HttpApythiaMock

        fun beforeEach() {
            underTest = HttpApythiaMock()
        }

        suspend fun FunSpecContainerScope.callOnceTest(
            act: HttpResponseMockBuilder.() -> Unit,
        ) {
            test("can be called only once") {
                underTest.mockNextResponse {
                    act()

                    shouldThrow<IllegalStateException> {
                        act()
                    }
                }
            }
        }

        fun requireActualResponse(): HttpResponse {
            return checkNotNull(underTest.actualResponse)
        }
    }
}

private fun FunSpec.dslExtensionConfigTests(fixture: HttpApythiaTest.Fixture) = with(fixture) {
    context("DSL extension config") {
        test("fail if same config type is set twice") {
            val config1 = DslExtensionConfigMock()
            val config2 = DslExtensionConfigMock()

            HttpApythiaMock {
                dslExtensionConfig(config1)
                shouldThrow<IllegalStateException> {
                    dslExtensionConfig(config2)
                }
            }
        }

        context("in ${HttpResponseMockBuilder::class.simpleName}") {
            dslExtensionConfigTestSuite(
                callDslExtensionConfigProvider = { mockNextResponse(it) }
            )
        }

        context("in ${UrlAssertion::class.simpleName}") {
            dslExtensionConfigTestSuite(
                callDslExtensionConfigProvider = {
                    assertNextRequest { url(it) }
                }
            )
        }

        context("in ${QueryAssertion::class.simpleName}") {
            dslExtensionConfigTestSuite(
                callDslExtensionConfigProvider = {
                    assertNextRequest {
                        url { query(it) }
                    }
                }
            )
        }

        context("in ${HeadersAssertion::class.simpleName}") {
            dslExtensionConfigTestSuite(
                callDslExtensionConfigProvider = {
                    assertNextRequest { headers(it) }
                }
            )
        }

        context("in ${BodyAssertion::class.simpleName}") {
            dslExtensionConfigTestSuite(
                callDslExtensionConfigProvider = {
                    assertNextRequest { body(it) }
                }
            )
        }
    }
}

private suspend fun FunSpecContainerScope.dslExtensionConfigTestSuite(
    callDslExtensionConfigProvider: suspend HttpApythia.(DslExtensionConfigProvider.() -> Unit) -> Unit,
) {
    test("set config and retrieve it") {
        val expected = Random.nextInt()
        val config = DslExtensionConfigMock().also { it.data = expected }

        HttpApythiaMock {
            dslExtensionConfig(object : DslExtensionConfig {})
            dslExtensionConfig(config)
        }.callDslExtensionConfigProvider {
            getDslExtensionConfig<DslExtensionConfigMock>()
                .let(::checkNotNull)
                .data
                .shouldBe(expected)
        }
    }

    test("get null config if not set") {
        HttpApythiaMock().callDslExtensionConfigProvider {
            getDslExtensionConfig<DslExtensionConfigMock>().shouldBeNull()
        }
    }
}

private fun FunSpec.assertionTests(fixture: HttpApythiaTest.Fixture) = with(fixture) {
    context("assertion") {
        methodTests(fixture)
        urlTests(fixture)
        queryTests(fixture)
        requestHeadersTests(fixture)
        requestBodyTests(fixture)
    }
}

private fun FunSpec.mockingTests(fixture: HttpApythiaTest.Fixture) = with(fixture) {
    context("mocking") {
        statusCodeTests(fixture)
        headersTests(fixture)
        bodyTests(fixture)

        test("set all values at once") {
            val expectedStatusCode = 404
            val expectedHeaders = mapOf("X-Custom-Header" to listOf("value"))
            val expectedBody = byteArrayOf(1, 2, 3)

            underTest.mockNextResponse {
                statusCode(expectedStatusCode)
                headers {
                    expectedHeaders.forEach { (name, values) -> headers(name, values) }
                }
                bytesBody(value = expectedBody, contentType = null)
            }

            val actualResponse = requireActualResponse()
            actualResponse.statusCode shouldBe expectedStatusCode
            actualResponse.headers shouldBe expectedHeaders
            actualResponse.body shouldBe expectedBody
        }
    }
}
