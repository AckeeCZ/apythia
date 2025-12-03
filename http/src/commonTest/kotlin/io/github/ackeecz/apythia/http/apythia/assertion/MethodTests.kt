package io.github.ackeecz.apythia.http.apythia.assertion

import io.github.ackeecz.apythia.http.apythia.HttpApythiaTest
import io.github.ackeecz.apythia.http.request.HttpMethod
import io.github.ackeecz.apythia.testing.http.shouldFail
import io.github.ackeecz.apythia.testing.http.shouldNotFail
import io.kotest.core.spec.style.scopes.FunSpecContainerScope

internal suspend fun FunSpecContainerScope.methodTests(
    fixture: HttpApythiaTest.Fixture
) = with(fixture) {
    context("method") {
        test("failure") {
            underTest.actualRequestMethod = "POST"

            shouldFail {
                underTest.assertNextRequest {
                    method(HttpMethod.GET)
                }
            }
        }

        test("success") {
            val expected = HttpMethod.GET
            underTest.actualRequestMethod = expected.value

            shouldNotFail {
                underTest.assertNextRequest {
                    method(expected)
                }
            }
        }

        test("success when actual method is in a different case") {
            val expected = HttpMethod.GET
            underTest.actualRequestMethod = expected.value.lowercase()

            shouldNotFail {
                underTest.assertNextRequest {
                    method(expected)
                }
            }
        }
    }
}
