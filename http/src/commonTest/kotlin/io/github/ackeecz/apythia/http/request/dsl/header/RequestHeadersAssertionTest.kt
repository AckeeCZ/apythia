package io.github.ackeecz.apythia.http.request.dsl.header

import io.github.ackeecz.apythia.http.request.dsl.HttpRequestAssertion
import io.github.ackeecz.apythia.http.request.dsl.HttpRequestAssertionImpl
import io.github.ackeecz.apythia.http.request.header.ExpectedHeaders
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull

internal class RequestHeadersAssertionTest : FunSpec({

    val fixture = Fixture()

    beforeEach {
        fixture.beforeEach()
    }

    with(fixture) {
        test("headers not asserted by default") {
            underTest.headers.shouldBeNull()
        }

        headersAssertionTestSuite(fixture)
    }
}) {

    class Fixture : HeadersAssertionFixture() {

        override val HttpRequestAssertionImpl.expectedHeaders: ExpectedHeaders
            get() = expectedRequest.headers.headers

        override fun HttpRequestAssertion.headersTest(test: HeadersAssertion.() -> Unit) {
            headers { test() }
        }
    }
}
