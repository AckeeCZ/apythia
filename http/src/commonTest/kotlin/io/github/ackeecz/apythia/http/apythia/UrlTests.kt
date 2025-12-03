package io.github.ackeecz.apythia.http.apythia

import io.github.ackeecz.apythia.http.request.dsl.url.QueryAssertion
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.spec.style.scopes.FunSpecContainerScope

internal fun FunSpec.urlTests(fixture: HttpApythiaTest.Fixture) = with(fixture) {
    context("url") {
        test("url failure") {
            underTest.actualUrl = "http://example.com"

            shouldFail {
                underTest.assertNextRequest {
                    url { url("http://another-example.com") }
                }
            }
        }

        test("url success") {
            val expected = "http://example.com/path?param=value"
            underTest.actualUrl = expected

            shouldNotFail {
                underTest.assertNextRequest {
                    url { url(expected) }
                }
            }
        }

        test("path failure") {
            underTest.actualUrl = "http://example.com"

            shouldFail {
                underTest.assertNextRequest {
                    url { path("/path") }
                }
            }
        }

        test("path success") {
            val expected = "/path/to/something"
            underTest.actualUrl = "http://example.com$expected"

            shouldNotFail {
                underTest.assertNextRequest {
                    url { path(expected) }
                }
            }
        }

        test("pathSuffix failure") {
            underTest.actualUrl = "http://example.com/path/to/something"

            shouldFail {
                underTest.assertNextRequest {
                    url { pathSuffix("to/something-else") }
                }
            }
        }

        test("pathSuffix success") {
            val expected = "to/something"
            underTest.actualUrl = "http://example.com/path/$expected"

            shouldNotFail {
                underTest.assertNextRequest {
                    url { pathSuffix(expected) }
                }
            }
        }

        queryTests(fixture)
    }
}

private suspend fun FunSpecContainerScope.queryTests(fixture: HttpApythiaTest.Fixture) = with(fixture) {
    context("query") {
        parameterWithoutValueTests(fixture)
        parameterTests(fixture)
        parametersTests(fixture)
        missingParametersTests(fixture)
        noParametersTests(fixture)

        test("order of different parameters does not matter") {
            val key1 = "key1"
            val value1 = "value1"
            val key2 = "key2"
            underTest.actualUrl = "http://example.com?$key1=$value1&$key2"

            shouldNotFail {
                underTest.assertNextRequest {
                    url {
                        query {
                            parameterWithoutValue(key2)
                            parameter(key1, value1)
                        }
                    }
                }
            }
        }

        test("order of parameters with the same key does not matter") {
            val key = "key"
            val value1 = "value1"
            val value2 = "value2"
            underTest.actualUrl = "http://example.com?$key=$value1&$key&$key=$value2"

            shouldNotFail {
                underTest.assertNextRequest {
                    url {
                        query {
                            parameterWithoutValue(key)
                            parameter(key, value2)
                            parameter(key, value1)
                        }
                    }
                }
            }
        }
    }
}

private suspend fun FunSpecContainerScope.parameterWithoutValueTests(
    fixture: HttpApythiaTest.Fixture
) = with(fixture) {
    context("parameterWithoutValue") {
        test("failure") {
            val expected = "key"
            underTest.actualUrl = "http://example.com?$expected=value"

            shouldFail {
                underTest.assertNextRequest {
                    url {
                        query { parameterWithoutValue(expected) }
                    }
                }
            }
        }

        suspend fun parameterWithoutValueSuccessTest(includeEqualsSign: Boolean) {
            val expected = "key"
            val equalsSign = if (includeEqualsSign) "=" else ""
            underTest.actualUrl = "http://example.com?$expected$equalsSign"

            shouldNotFail {
                underTest.assertNextRequest {
                    url {
                        query { parameterWithoutValue(expected) }
                    }
                }
            }
        }

        test("success with missing value and =") {
            parameterWithoutValueSuccessTest(includeEqualsSign = true)
        }

        test("success with missing value but present =") {
            parameterWithoutValueSuccessTest(includeEqualsSign = false)
        }
    }
}

private suspend fun FunSpecContainerScope.parameterTests(
    fixture: HttpApythiaTest.Fixture
) = with(fixture) {
    context("parameter") {
        context("String") {
            val expectedValue = "value"
            parameterTestSuite(
                fixture = fixture,
                expectedValue = expectedValue,
                assertValue = { key -> parameter(key, expectedValue) },
            )
        }

        context("Int") {
            val expectedValue = 1
            parameterTestSuite(
                fixture = fixture,
                expectedValue = expectedValue.toString(),
                assertValue = { key -> parameter(key, expectedValue) },
            )
        }

        context("Long") {
            val expectedValue = 1L
            parameterTestSuite(
                fixture = fixture,
                expectedValue = expectedValue.toString(),
                assertValue = { key -> parameter(key, expectedValue) },
            )
        }

        context("Double") {
            val expectedValue = 1.0
            parameterTestSuite(
                fixture = fixture,
                expectedValue = expectedValue.toString(),
                assertValue = { key -> parameter(key, expectedValue) },
            )
        }
    }
}

private suspend fun FunSpecContainerScope.parameterTestSuite(
    fixture: HttpApythiaTest.Fixture,
    expectedValue: String,
    assertValue: QueryAssertion.(key: String) -> Unit,
) = with(fixture) {
    test("failure") {
        val key = "key"
        val actualValue = "$expectedValue-failure"
        underTest.actualUrl = "http://example.com?$key=$actualValue"

        shouldFail {
            underTest.assertNextRequest {
                url {
                    query { assertValue(key) }
                }
            }
        }
    }

    test("success") {
        val key = "key"
        underTest.actualUrl = "http://example.com?$key=$expectedValue"

        shouldNotFail {
            underTest.assertNextRequest {
                url {
                    query { assertValue(key) }
                }
            }
        }
    }

    test("multiple values success") {
        val key1 = "key1"
        val key2 = "key2"
        underTest.actualUrl = "http://example.com?$key1=$expectedValue&$key2=$expectedValue"

        shouldNotFail {
            underTest.assertNextRequest {
                url {
                    query {
                        parameter(key1, expectedValue)
                        parameter(key2, expectedValue)
                    }
                }
            }
        }
    }
}

private suspend fun FunSpecContainerScope.parametersTests(
    fixture: HttpApythiaTest.Fixture
) = with(fixture) {
    context("parameters") {
        test("failure") {
            val key = "key"
            val expectedValue1 = "value1"
            val expectedValue2 = "value2"
            val expectedValues = listOf(expectedValue1, expectedValue2)
            underTest.actualUrl = "http://example.com?$key=$expectedValue1"

            shouldFail {
                underTest.assertNextRequest {
                    url {
                        query { parameters(key, expectedValues) }
                    }
                }
            }
        }

        test("success") {
            val key = "key"
            val expectedValue1 = "value1"
            val expectedValue2 = "value2"
            val expectedValues = listOf(expectedValue1, expectedValue2)
            underTest.actualUrl = "http://example.com?$key=$expectedValue1&$key=$expectedValue2"

            shouldNotFail {
                underTest.assertNextRequest {
                    url {
                        query { parameters(key, expectedValues) }
                    }
                }
            }
        }
    }
}

private suspend fun FunSpecContainerScope.missingParametersTests(
    fixture: HttpApythiaTest.Fixture
) = with(fixture) {
    context("missingParameters") {
        test("failure") {
            val expected = "key"
            underTest.actualUrl = "http://example.com?$expected=value"

            shouldFail {
                underTest.assertNextRequest {
                    url {
                        query { missingParameters(expected) }
                    }
                }
            }
        }

        test("success") {
            val expected = "key"
            underTest.actualUrl = "http://example.com"

            shouldNotFail {
                underTest.assertNextRequest {
                    url {
                        query { missingParameters(expected) }
                    }
                }
            }
        }
    }
}

private suspend fun FunSpecContainerScope.noParametersTests(
    fixture: HttpApythiaTest.Fixture
) = with(fixture) {
    context("noParameters") {
        test("failure") {
            underTest.actualUrl = "http://example.com?key=value"

            shouldFail {
                underTest.assertNextRequest {
                    url {
                        query { noParameters() }
                    }
                }
            }
        }

        test("success") {
            underTest.actualUrl = "http://example.com"

            shouldNotFail {
                underTest.assertNextRequest {
                    url {
                        query { noParameters() }
                    }
                }
            }
        }
    }
}
