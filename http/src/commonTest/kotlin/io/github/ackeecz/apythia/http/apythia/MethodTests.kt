package io.github.ackeecz.apythia.http.apythia

import io.github.ackeecz.apythia.http.request.HttpMethod
import io.kotest.core.spec.style.FunSpec

internal fun FunSpec.methodTests(fixture: HttpApythiaTest.Fixture) = with(fixture) {
    context("method") {
        test("failure") {
            underTest.actualMethod = "POST"

            shouldFail {
                underTest.assertNextRequest {
                    method(HttpMethod.GET)
                }
            }
        }

        test("success") {
            val expected = HttpMethod.GET
            underTest.actualMethod = expected.value

            shouldNotFail {
                underTest.assertNextRequest {
                    method(expected)
                }
            }
        }

        test("success when actual method is in a different case") {
            val expected = HttpMethod.GET
            underTest.actualMethod = expected.value.lowercase()

            shouldNotFail {
                underTest.assertNextRequest {
                    method(expected)
                }
            }
        }
    }
}
