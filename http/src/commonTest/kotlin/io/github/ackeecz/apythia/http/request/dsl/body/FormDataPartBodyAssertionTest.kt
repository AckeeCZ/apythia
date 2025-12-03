package io.github.ackeecz.apythia.http.request.dsl.body

import io.github.ackeecz.apythia.http.request.body.ExpectedBody
import io.github.ackeecz.apythia.http.request.dsl.HttpRequestAssertion
import io.github.ackeecz.apythia.http.request.dsl.HttpRequestAssertionImpl
import io.kotest.core.spec.style.FunSpec

internal class FormDataPartBodyAssertionTest : FunSpec({

    val fixture = Fixture()

    beforeEach {
        fixture.beforeEach()
    }

    bodyAssertionTestSuite(fixture)
}) {

    class Fixture : BodyAssertionFixture() {

        override val HttpRequestAssertionImpl.expectedBody: ExpectedBody?
            get() = firstFormDataPartBody.body

        override fun HttpRequestAssertion.bodyTest(test: BodyAssertion.() -> Unit) {
            body {
                multipartFormData {
                    part("name") {
                        body { test() }
                    }
                }
            }
        }
    }
}
