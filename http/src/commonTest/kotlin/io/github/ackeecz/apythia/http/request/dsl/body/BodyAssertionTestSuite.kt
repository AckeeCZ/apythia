@file:Suppress("MatchingDeclarationName")

package io.github.ackeecz.apythia.http.request.dsl.body

import io.github.ackeecz.apythia.http.extension.HttpDslExtensionMock
import io.github.ackeecz.apythia.http.request.body.ExpectedBody
import io.github.ackeecz.apythia.http.request.dsl.HttpRequestAssertion
import io.github.ackeecz.apythia.http.request.dsl.HttpRequestAssertionImpl
import io.github.ackeecz.apythia.http.request.dsl.createHttpRequestAssertionImpl
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.spec.style.scopes.FunSpecContainerScope
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

internal abstract class BodyAssertionFixture {

    lateinit var underTest: HttpRequestAssertionImpl

    abstract val HttpRequestAssertionImpl.expectedBody: ExpectedBody?

    fun beforeEach() {
        underTest = createHttpRequestAssertionImpl()
    }

    abstract fun HttpRequestAssertion.bodyTest(test: BodyAssertion.() -> Unit)
}

internal fun FunSpec.bodyAssertionTestSuite(fixture: BodyAssertionFixture) = with(fixture) {
    test("body not asserted when just empty body assertion block is called") {
        underTest.bodyTest {}

        underTest.expectedBody.shouldBeNull()
    }

    emptyTests(fixture)
    bytesTests(fixture)
    plainTextTests(fixture)
    formDataMultipartTests(fixture)
    partialFormDataMultipartTests(fixture)
    dslExtensionTests(fixture)
}

private fun FunSpec.emptyTests(fixture: BodyAssertionFixture) = with(fixture) {
    context("empty") {
        test("set") {
            underTest.bodyTest { empty() }

            underTest.expectedBody shouldBe ExpectedBody.Empty
        }

        callOnceTest(fixture) { empty() }
    }
}

private fun FunSpec.bytesTests(fixture: BodyAssertionFixture) = with(fixture) {
    context("bytes") {
        test("set") {
            val value = byteArrayOf(1, 2, 3)

            underTest.bodyTest { bytes(value) }

            underTest.expectedBody shouldBe ExpectedBody.Bytes(value)
        }

        callOnceTest(fixture) { bytes(byteArrayOf()) }
    }
}

private fun FunSpec.plainTextTests(fixture: BodyAssertionFixture) = with(fixture) {
    context("plainText (String)") {
        plainTextTestSuite(
            fixture = fixture,
            createValueFrom = { it.toString() },
            callPlainText = { plainText(it) },
        )
    }
    context("plainText (Int)") {
        plainTextTestSuite(
            fixture = fixture,
            createValueFrom = { it },
            callPlainText = { plainText(it) },
        )
    }
    context("plainText (Double)") {
        plainTextTestSuite(
            fixture = fixture,
            createValueFrom = { it.toDouble() },
            callPlainText = { plainText(it) },
        )
    }
}

private suspend fun <T> FunSpecContainerScope.plainTextTestSuite(
    fixture: BodyAssertionFixture,
    createValueFrom: (Int) -> T,
    callPlainText: BodyAssertion.(T) -> Unit,
) = with(fixture) {
    test("set") {
        val value = createValueFrom(123)

        underTest.bodyTest { callPlainText(value) }

        underTest.expectedBody shouldBe ExpectedBody.PlainText(value.toString())
    }

    callOnceTest(fixture) { callPlainText(createValueFrom(123)) }
}

private fun FunSpec.formDataMultipartTests(fixture: BodyAssertionFixture) = with(fixture) {
    context("formDataMultipart") {
        callOnceTest(fixture) {
            multipartFormData {
                part("name") {}
            }
        }

        test("fail when just empty formDataMultipart is called") {
            shouldThrow<IllegalStateException> {
                underTest.body { multipartFormData {} }
            }
        }
    }
}

private fun FunSpec.partialFormDataMultipartTests(fixture: BodyAssertionFixture) = with(fixture) {
    context("partialFormDataMultipart") {
        callOnceTest(fixture) { partialMultipartFormData {} }
    }
}

private fun FunSpec.dslExtensionTests(fixture: BodyAssertionFixture) = with(fixture) {
    context("dslExtension") {
        callOnceTest(fixture) { dslExtension(HttpDslExtensionMock()) }

        test("set") {
            val expectedExtension = HttpDslExtensionMock()

            underTest.bodyTest { dslExtension(expectedExtension) }

            underTest.expectedBody shouldBe ExpectedBody.DslExtension(expectedExtension)
        }
    }
}

private suspend fun FunSpecContainerScope.callOnceTest(
    fixture: BodyAssertionFixture,
    act: BodyAssertion.() -> Unit,
) = with(fixture) {
    test("can be called only once") {
        underTest.bodyTest {
            act()

            shouldThrow<IllegalStateException> {
                act()
            }
        }
    }
}
