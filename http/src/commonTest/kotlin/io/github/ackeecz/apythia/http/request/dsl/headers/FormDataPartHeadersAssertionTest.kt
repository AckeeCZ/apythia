package io.github.ackeecz.apythia.http.request.dsl.headers

import io.github.ackeecz.apythia.http.request.dsl.HttpRequestAssertion
import io.github.ackeecz.apythia.http.request.dsl.HttpRequestAssertionImpl
import io.github.ackeecz.apythia.http.request.dsl.body.firstFormDataPartHeaders
import io.github.ackeecz.apythia.http.request.headers.ExpectedHeaders
import io.kotest.core.spec.style.FunSpec

internal class FormDataPartHeadersAssertionTest : FunSpec({

    val fixture = Fixture()

    beforeEach {
        fixture.beforeEach()
    }

    headersAssertionTestSuite(fixture)
}) {

    class Fixture : HeadersAssertionFixture() {

        override val HttpRequestAssertionImpl.expectedHeaders: ExpectedHeaders
            get() = firstFormDataPartHeaders.headers

        override fun HttpRequestAssertion.headersTest(test: HeadersAssertion.() -> Unit) {
            body {
                multipartFormData {
                    part("name") {
                        headers { test() }
                    }
                }
            }
        }
    }
}
