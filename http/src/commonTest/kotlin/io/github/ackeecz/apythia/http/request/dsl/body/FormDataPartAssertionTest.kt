package io.github.ackeecz.apythia.http.request.dsl.body

import io.github.ackeecz.apythia.http.request.body.ExpectedFormDataPart
import io.github.ackeecz.apythia.http.request.dsl.HttpRequestAssertionImpl
import io.github.ackeecz.apythia.http.request.dsl.createHttpRequestAssertionImpl
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.spec.style.scopes.FunSpecContainerScope
import io.kotest.matchers.nulls.shouldBeNull

private lateinit var underTest: HttpRequestAssertionImpl

class FormDataPartAssertionTest : FunSpec({

    beforeEach {
        underTest = createHttpRequestAssertionImpl()
    }

    context("headers") {
        callOnceTest { headers {} }

        test("not asserted when just empty part is called") {
            underTest.body {
                multipartFormData {
                    part("name") {}
                }
            }

            underTest.firstFormDataPartHeaders
                .headers
                .headers
                .shouldBeNull()
        }
    }

    context("body") {
        callOnceTest { body {} }

        test("not asserted when just empty part is called") {
            underTest.body {
                multipartFormData {
                    part("name") {}
                }
            }

            underTest.firstFormDataPartBody
                .body
                .shouldBeNull()
        }
    }
})

private suspend fun FunSpecContainerScope.callOnceTest(
    act: FormDataPartAssertion.() -> Unit,
) {
    test("can be called only once") {
        underTest.body {
            multipartFormData {
                part("name") {
                    act()

                    shouldThrow<IllegalStateException> {
                        act()
                    }
                }
            }
        }
    }
}

internal val HttpRequestAssertionImpl.firstFormDataPartHeaders: ExpectedFormDataPart.Headers
    get() = expectedFormDataParts.first().headers

internal val HttpRequestAssertionImpl.firstFormDataPartBody: ExpectedFormDataPart.Body
    get() = expectedFormDataParts.first().body
