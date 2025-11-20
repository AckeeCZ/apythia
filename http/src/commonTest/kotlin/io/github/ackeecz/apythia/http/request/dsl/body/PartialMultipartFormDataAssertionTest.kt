package io.github.ackeecz.apythia.http.request.dsl.body

import io.github.ackeecz.apythia.http.request.body.ExpectedBody
import io.github.ackeecz.apythia.http.request.dsl.HttpRequestAssertionImpl
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.types.shouldBeInstanceOf

private lateinit var underTest: HttpRequestAssertionImpl

class PartialMultipartFormDataAssertionTest : FunSpec({

    beforeEach {
        underTest = HttpRequestAssertionImpl()
    }

    context("part") {
        multipartFormDataAssertionTestSuite(
            getUnderTest = { underTest },
            callMultipartAssertion = { partialMultipartFormData(it) },
            getExpectedFormDataParts = { checkNotNull(underTest.expectedPartialFormDataParts) },
        )

        test("not asserted when not called") {
            underTest.body {
                partialMultipartFormData {
                    missingParts("name")
                }
            }

            underTest.expectedPartialFormDataParts.shouldBeNull()
        }
    }

    context("missingParts") {
        test("not asserted when not called") {
            underTest.body {
                partialMultipartFormData {
                    part("name") {}
                }
            }

            underTest.expectedMissingParts.shouldBeNull()
        }

        test("set missing parts") {
            val expectedMissingParts = listOf("name", "other_name")

            underTest.body {
                partialMultipartFormData {
                    missingParts(*expectedMissingParts.toTypedArray())
                }
            }

            underTest.expectedMissingParts
                .shouldNotBeNull()
                .shouldHaveSize(expectedMissingParts.size)
                .shouldContainAll(expectedMissingParts)
        }
    }
})

internal val HttpRequestAssertionImpl.expectedPartialMultipartFormData
    get() = expectedRequest.body
        .body
        .shouldBeInstanceOf<ExpectedBody.PartialMultipartFormData>()

private val HttpRequestAssertionImpl.expectedPartialFormDataParts
    get() = expectedPartialMultipartFormData.parts

private val HttpRequestAssertionImpl.expectedMissingParts
    get() = expectedPartialMultipartFormData.missingParts
