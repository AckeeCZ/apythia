package io.github.ackeecz.apythia.http.request.dsl

import io.github.ackeecz.apythia.http.callOnceTest
import io.github.ackeecz.apythia.http.request.HttpMethod
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

private lateinit var underTest: HttpRequestAssertionImpl

class HttpRequestAssertionTest : FunSpec({

    beforeEach {
        underTest = HttpRequestAssertionImpl()
    }

    context("method") {
        test("is missing by default") {
            underTest.expectedRequest.method.shouldBeNull()
        }

        test("set") {
            val expectedMethod = HttpMethod.entries.random()

            underTest.method(expectedMethod)

            underTest.expectedRequest.method shouldBe expectedMethod
        }

        callOnceTest { underTest.method(HttpMethod.GET) }
    }

    context("url") {
        callOnceTest { underTest.url {} }
    }

    context("headers") {
        callOnceTest { underTest.headers {} }
    }

    context("body") {
        callOnceTest { underTest.body {} }
    }
})
