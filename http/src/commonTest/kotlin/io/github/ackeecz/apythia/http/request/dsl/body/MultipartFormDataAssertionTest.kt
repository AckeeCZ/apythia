package io.github.ackeecz.apythia.http.request.dsl.body

import io.github.ackeecz.apythia.http.request.body.ExpectedBody
import io.github.ackeecz.apythia.http.request.body.ExpectedFormDataPart
import io.github.ackeecz.apythia.http.request.dsl.HttpRequestAssertion
import io.github.ackeecz.apythia.http.request.dsl.HttpRequestAssertionImpl
import io.github.ackeecz.apythia.http.request.dsl.createHttpRequestAssertionImpl
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.spec.style.scopes.FunSpecContainerScope
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

private lateinit var underTest: HttpRequestAssertionImpl

class MultipartFormDataAssertionTest : FunSpec({

    beforeEach {
        underTest = createHttpRequestAssertionImpl()
    }

    context("part") {
        multipartFormDataAssertionTestSuite(
            getUnderTest = { underTest },
            callMultipartAssertion = { multipartFormData(it) },
            getExpectedFormDataParts = { underTest.expectedFormDataParts },
        )
    }
})

internal suspend fun FunSpecContainerScope.multipartFormDataAssertionTestSuite(
    getUnderTest: () -> HttpRequestAssertion,
    callMultipartAssertion: RequestBodyAssertion.(MultipartFormDataAssertion.() -> Unit) -> Unit = {},
    getExpectedFormDataParts: () -> List<ExpectedFormDataPart>,
) {
    test("add with name and default null filename") {
        val expectedName = "expected_name"

        getUnderTest().body {
            callMultipartAssertion {
                part(name = expectedName) {}
            }
        }

        val expectedParts = getExpectedFormDataParts()
        expectedParts shouldHaveSize 1
        with(expectedParts.first()) {
            name shouldBe expectedName
            filename.shouldBeNull()
        }
    }

    test("add with provided filename") {
        val expectedFilename = "expected_filename"

        getUnderTest().body {
            callMultipartAssertion {
                part(name = "name", filename = expectedFilename) {}
            }
        }

        getExpectedFormDataParts().first().filename shouldBe expectedFilename
    }

    test("add two parts with same name") {
        val expectedName = "expected_name"

        getUnderTest().body {
            callMultipartAssertion {
                part(name = expectedName) {}
                part(name = expectedName) {}
            }
        }

        getExpectedFormDataParts() shouldHaveSize 2
    }
}

internal val HttpRequestAssertionImpl.expectedFormDataParts
    get() = expectedRequest.body
        .body
        .shouldBeInstanceOf<ExpectedBody.MultipartFormData>()
        .parts
