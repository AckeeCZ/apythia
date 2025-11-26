package io.github.ackeecz.apythia.http.apythia

import io.github.ackeecz.apythia.http.HttpApythia
import io.github.ackeecz.apythia.http.apythia.assertion.bodyTests
import io.github.ackeecz.apythia.http.apythia.assertion.methodTests
import io.github.ackeecz.apythia.http.apythia.assertion.rootHeadersTests
import io.github.ackeecz.apythia.http.apythia.assertion.urlTests
import io.github.ackeecz.apythia.http.extension.DslExtensionConfig
import io.github.ackeecz.apythia.http.extension.DslExtensionConfigMock
import io.github.ackeecz.apythia.http.extension.DslExtensionConfigProvider
import io.github.ackeecz.apythia.http.extension.getDslExtensionConfig
import io.github.ackeecz.apythia.http.request.dsl.body.BodyAssertion
import io.github.ackeecz.apythia.http.response.dsl.HttpResponseArrangement
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
}) {

    class Fixture {

        lateinit var underTest: HttpApythiaMock

        fun beforeEach() {
            underTest = HttpApythiaMock()
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
        test("don't assert anything by default if not explicitly specified") {
            fixture.underTest.assertNextRequest {}
        }

        methodTests(fixture)
        urlTests(fixture)
        rootHeadersTests(fixture)
        bodyTests(fixture)
    }
}
