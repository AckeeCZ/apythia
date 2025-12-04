package io.github.ackeecz.apythia.http.apythia.assertion

import io.github.ackeecz.apythia.http.apythia.HttpApythiaTest
import io.github.ackeecz.apythia.http.request.dsl.url.QueryAssertion
import io.github.ackeecz.apythia.testing.http.shouldFail
import io.github.ackeecz.apythia.testing.http.shouldNotFail
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.scopes.FunSpecContainerScope
import io.kotest.matchers.shouldBe

internal suspend fun FunSpecContainerScope.queryTests(
    fixture: HttpApythiaTest.Fixture
) = with(fixture) {
    context("query") {
        noValueParameterTests(fixture)
        parameterTests(fixture)
        parametersTests(fixture)
        missingParametersTests(fixture)
        noParametersTests(fixture)

        test("order of parameters does not matter") {
            val key1 = "key1"
            val value1 = "value1"
            val key2 = "key2"
            underTest.actualRequestUrl = "http://example.com?$key1=$value1&$key2"

            shouldNotFail {
                underTest.assertNextRequest {
                    url {
                        query {
                            noValueParameter(key2)
                            parameter(key1, value1)
                        }
                    }
                }
            }
        }

        test("parameters assertions can be called together except noParameters") {
            underTest.actualRequestUrl = "http://example.com?key1&key2=value&key3=1&key4=1&key5=1.0&key6=value1&key6=value2"
            underTest.assertNextRequest {
                url {
                    query {
                        shouldNotThrow<IllegalStateException> {
                            noValueParameter("key1")
                            parameter("key2", "value")
                            parameter("key3", 1)
                            parameter("key4", 1L)
                            parameter("key5", 1.0)
                            parameters("key6", listOf("value1", "value2"))
                            missingParameters("key7")
                        }
                    }
                }
            }
        }

        test("get actual query parameters") {
            val expected = mapOf(
                "key" to listOf("value1", "value2"),
                "key2" to listOf("value3"),
                "key3" to listOf(null, "value4"),
            )
            underTest.actualRequestUrl = "http://example.com/path?key=value1&key=value2&key2=value3&key3&key3=value4"

            underTest.assertNextRequest {
                url {
                    query {
                        actualQueryParameters shouldBe expected
                    }
                }
            }
        }

        test("block can be called only once") {
            underTest.assertNextRequest {
                url {
                    query {}

                    shouldThrow<IllegalStateException> {
                        query {}
                    }
                }
            }
        }
    }
}

private suspend fun FunSpecContainerScope.noValueParameterTests(
    fixture: HttpApythiaTest.Fixture
) = with(fixture) {
    context("noValueParameter") {
        test("fail when parameter missing") {
            val expected = "key"
            underTest.actualRequestUrl = "http://example.com"

            shouldFail {
                underTest.assertNextRequest {
                    url {
                        query { noValueParameter(expected) }
                    }
                }
            }
        }

        test("succeed when there are more values") {
            val expected = "key"
            underTest.actualRequestUrl = "http://example.com?$expected&$expected"

            shouldNotFail {
                underTest.assertNextRequest {
                    url {
                        query { noValueParameter(expected) }
                    }
                }
            }
        }

        test("fail when has value") {
            val expected = "key"
            underTest.actualRequestUrl = "http://example.com?$expected=value"

            shouldFail {
                underTest.assertNextRequest {
                    url {
                        query { noValueParameter(expected) }
                    }
                }
            }
        }

        suspend fun noParameterValueSuccessTest(includeEqualsSign: Boolean) {
            val expected = "key"
            val equalsSign = if (includeEqualsSign) "=" else ""
            underTest.actualRequestUrl = "http://example.com?$expected$equalsSign"

            shouldNotFail {
                underTest.assertNextRequest {
                    url {
                        query { noValueParameter(expected) }
                    }
                }
            }
        }

        test("success with missing value and =") {
            noParameterValueSuccessTest(includeEqualsSign = true)
        }

        test("success with missing value but present =") {
            noParameterValueSuccessTest(includeEqualsSign = false)
        }
    }
}

private suspend fun FunSpecContainerScope.parameterTests(
    fixture: HttpApythiaTest.Fixture
) = with(fixture) {
    context("parameter") {
        context("String") {
            parameterTestSuite(
                fixture = fixture,
                getValue = { it.toString() },
                assertValue = { key, value -> parameter(key, value) },
            )
        }

        context("Int") {
            parameterTestSuite(
                fixture = fixture,
                getValue = { it },
                assertValue = { key, value -> parameter(key, value) },
            )
        }

        context("Long") {
            parameterTestSuite(
                fixture = fixture,
                getValue = { it.toLong() },
                assertValue = { key, value -> parameter(key, value) },
            )
        }

        context("Double") {
            parameterTestSuite(
                fixture = fixture,
                getValue = { it.toDouble() },
                assertValue = { key, value -> parameter(key, value) },
            )
        }
    }
}

private suspend fun <T> FunSpecContainerScope.parameterTestSuite(
    fixture: HttpApythiaTest.Fixture,
    getValue: (Int) -> T,
    assertValue: QueryAssertion.(key: String, value: T) -> Unit,
) = with(fixture) {
    test("fail when parameter missing") {
        underTest.actualRequestUrl = "http://example.com"

        shouldFail {
            underTest.assertNextRequest {
                url {
                    query { assertValue("key", getValue(1)) }
                }
            }
        }
    }

    test("succeed when there are more values and at least one matches") {
        val value1 = getValue(1)
        val value2 = getValue(2)
        underTest.actualRequestUrl = "http://example.com?key=$value1&key=$value2"

        shouldNotFail {
            underTest.assertNextRequest {
                url {
                    query { assertValue("key", value1) }
                }
            }
        }
    }

    test("fail when value is different") {
        val key = "key"
        val expectedValue = getValue(1)
        val actualValue = "$expectedValue-failure"
        underTest.actualRequestUrl = "http://example.com?$key=$actualValue"

        shouldFail {
            underTest.assertNextRequest {
                url {
                    query { assertValue(key, expectedValue) }
                }
            }
        }
    }

    test("success") {
        val key = "key"
        val expectedValue = getValue(1)
        underTest.actualRequestUrl = "http://example.com?$key=$expectedValue"

        shouldNotFail {
            underTest.assertNextRequest {
                url {
                    query { assertValue(key, expectedValue) }
                }
            }
        }
    }

    test("multiple values with different keys success") {
        val key1 = "key1"
        val key2 = "key2"
        val expectedValue = getValue(1)
        underTest.actualRequestUrl = "http://example.com?$key1=$expectedValue&$key2=$expectedValue"

        shouldNotFail {
            underTest.assertNextRequest {
                url {
                    query {
                        assertValue(key1, expectedValue)
                        assertValue(key2, expectedValue)
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
        test("fail when all values are missing") {
            val key = "key"
            val expectedValue1 = "value1"
            val expectedValue2 = "value2"
            val expectedValues = listOf(expectedValue1, expectedValue2)
            underTest.actualRequestUrl = "http://example.com"

            shouldFail {
                underTest.assertNextRequest {
                    url {
                        query { parameters(key, expectedValues) }
                    }
                }
            }
        }

        test("fail when some values are missing") {
            val key = "key"
            val expectedValue1 = "value1"
            val expectedValue2 = "value2"
            val expectedValues = listOf(expectedValue1, expectedValue2)
            underTest.actualRequestUrl = "http://example.com?$key=$expectedValue1"

            shouldFail {
                underTest.assertNextRequest {
                    url {
                        query { parameters(key, expectedValues) }
                    }
                }
            }
        }

        test("fail on empty list") {
            shouldThrow<IllegalArgumentException> {
                underTest.assertNextRequest {
                    url {
                        query {
                            parameters("key", emptyList())
                        }
                    }
                }
            }
        }

        test("succeed when all values are present") {
            val key = "key"
            val expectedValue1 = "value1"
            val expectedValue2 = "value2"
            val expectedValue3 = null
            val expectedValues = listOf(
                expectedValue1,
                expectedValue2,
                expectedValue2,
                expectedValue3,
                expectedValue3,
            ).shuffled()
            underTest.actualRequestUrl = "http://example.com?$key=$expectedValue1&$key=$expectedValue2&$key=$expectedValue2&$key&$key"

            shouldNotFail {
                underTest.assertNextRequest {
                    url {
                        query { parameters(key, expectedValues) }
                    }
                }
            }
        }

        test("succeed when just a subset of values are present") {
            val key = "key"
            val expectedValue1 = "value1"
            val expectedValue2 = null
            val expectedValues = listOf(expectedValue1, expectedValue2)
            underTest.actualRequestUrl = "http://example.com?$key=$expectedValue1&$key=extraValue&$key"

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
            underTest.actualRequestUrl = "http://example.com?$expected=value"

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
            underTest.actualRequestUrl = "http://example.com"

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
            underTest.actualRequestUrl = "http://example.com?key=value"

            shouldFail {
                underTest.assertNextRequest {
                    url {
                        query { noParameters() }
                    }
                }
            }
        }

        test("success") {
            underTest.actualRequestUrl = "http://example.com"

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
