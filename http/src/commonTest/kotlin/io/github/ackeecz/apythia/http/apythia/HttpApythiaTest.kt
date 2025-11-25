package io.github.ackeecz.apythia.http.apythia

import io.github.ackeecz.apythia.http.HttpApythia
import io.github.ackeecz.apythia.http.HttpApythiaImpl
import io.github.ackeecz.apythia.http.apythia.assertion.bodyTests
import io.github.ackeecz.apythia.http.apythia.assertion.methodTests
import io.github.ackeecz.apythia.http.apythia.assertion.rootHeadersTests
import io.github.ackeecz.apythia.http.apythia.assertion.urlTests
import io.github.ackeecz.apythia.http.extension.DslExtensionConfigProvider
import io.github.ackeecz.apythia.http.extension.HttpDslExtension
import io.github.ackeecz.apythia.http.extension.HttpDslExtensionMock
import io.github.ackeecz.apythia.http.extension.getDslExtensionConfig
import io.github.ackeecz.apythia.http.request.dsl.body.BodyAssertion
import io.github.ackeecz.apythia.http.response.dsl.HttpResponseArrangement
import io.kotest.assertions.throwables.shouldNotThrow
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
}) {

    class Fixture {

        lateinit var underTest: HttpApythiaImpl

        fun beforeEach() {
            underTest = HttpApythiaImpl()
        }

        inline fun shouldNotFail(block: () -> Unit) {
            shouldNotThrow<AssertionError> { block() }
        }

        inline fun shouldFail(block: () -> Unit) {
            shouldThrow<AssertionError> { block() }
        }
    }
}

private fun FunSpec.dslExtensionConfigTests(fixture: HttpApythiaTest.Fixture) = with(fixture) {
    context("DSL extension config") {
        test("fail if same config type is set twice") {
            val config1 = HttpDslExtensionMock.Config()
            val config2 = HttpDslExtensionMock.Config()

            HttpApythiaImpl {
                dslExtensionConfig(config1)
                shouldThrow<IllegalStateException> {
                    dslExtensionConfig(config2)
                }
            }
        }

        context("in ${BodyAssertion::class.simpleName}") {
            dslExtensionConfigTestSuite(
                callDslExtensionConfigProvider = {
                    assertNextRequest { body(it) }
                }
            )
        }

        context("in ${HttpResponseArrangement::class.simpleName}") {
            dslExtensionConfigTestSuite(
                callDslExtensionConfigProvider = { arrangeNextResponse(it) }
            )
        }
    }
}

private suspend fun FunSpecContainerScope.dslExtensionConfigTestSuite(
    callDslExtensionConfigProvider: suspend HttpApythia.(DslExtensionConfigProvider.() -> Unit) -> Unit,
) {
    test("set config and retrieve it") {
        val expected = Random.nextInt()
        val config = HttpDslExtensionMock.Config().also { it.data = expected }

        HttpApythiaImpl {
            dslExtensionConfig(object : HttpDslExtension.Config {})
            dslExtensionConfig(config)
        }.callDslExtensionConfigProvider {
            getDslExtensionConfig<HttpDslExtensionMock.Config>()
                .let(::checkNotNull)
                .data
                .shouldBe(expected)
        }
    }

    test("get null config if not set") {
        HttpApythiaImpl().callDslExtensionConfigProvider {
            getDslExtensionConfig<HttpDslExtensionMock.Config>().shouldBeNull()
        }
    }
}

private fun FunSpec.assertionTests(fixture: HttpApythiaTest.Fixture) = with(fixture) {
    context("assertion") {
        test("don't assert anything by default if not explicitly specified") {
            fixture.underTest.assertNextRequest {}
        }

        methodTests(fixture)
        urlTests(fixture)
        rootHeadersTests(fixture)
        bodyTests(fixture)
    }
}
