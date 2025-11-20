package io.github.ackeecz.apythia.http.request.dsl.body

import io.github.ackeecz.apythia.http.request.body.ExpectedBody
import io.github.ackeecz.apythia.http.request.dsl.HttpRequestAssertion
import io.github.ackeecz.apythia.http.request.dsl.HttpRequestAssertionImpl
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull

internal class RequestBodyAssertionTest : FunSpec({

    val fixture = Fixture()

    beforeEach {
        fixture.beforeEach()
    }

    with(fixture) {
        test("body not asserted by default") {
            underTest.expectedBody.shouldBeNull()
        }

        bodyAssertionTestSuite(fixture)
    }
}) {

    class Fixture : BodyAssertionFixture() {

        override val HttpRequestAssertionImpl.expectedBody: ExpectedBody?
            get() = expectedRequest.body.body

        override fun HttpRequestAssertion.bodyTest(test: BodyAssertion.() -> Unit) {
            body { test() }
        }
    }
}
