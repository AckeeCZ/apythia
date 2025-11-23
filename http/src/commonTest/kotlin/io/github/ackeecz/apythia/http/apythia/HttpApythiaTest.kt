package io.github.ackeecz.apythia.http.apythia

import io.github.ackeecz.apythia.http.HttpApythiaImpl
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec

internal class HttpApythiaTest : FunSpec({

    val fixture = Fixture()

    beforeEach {
        fixture.beforeEach()
    }

    test("don't assert anything by default if not explicitly specified") {
        fixture.underTest.assertNextRequest {}
    }

    methodTests(fixture)
    urlTests(fixture)
    headersTests(fixture)
    bodyTests(fixture)
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
