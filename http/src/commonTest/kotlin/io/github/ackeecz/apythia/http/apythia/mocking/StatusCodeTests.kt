package io.github.ackeecz.apythia.http.apythia.mocking

import io.github.ackeecz.apythia.http.apythia.HttpApythiaTest
import io.kotest.core.spec.style.scopes.FunSpecContainerScope
import io.kotest.matchers.shouldBe

internal suspend fun FunSpecContainerScope.statusCodeTests(
    fixture: HttpApythiaTest.Fixture
) = with(fixture) {
    context("status code") {
        callOnceTest { statusCode(200) }

        test("default is 200") {
            underTest.mockNextResponse {}

            requireActualResponse().statusCode shouldBe 200
        }

        test("set") {
            val expectedStatusCode = 404

            underTest.mockNextResponse {
                statusCode(expectedStatusCode)
            }

            requireActualResponse().statusCode shouldBe expectedStatusCode
        }
    }
}
