package io.github.ackeecz.apythia.http.request.dsl.url

import io.github.ackeecz.apythia.http.request.dsl.HttpRequestAssertionImpl
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.spec.style.scopes.FunSpecContainerScope
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

private lateinit var underTest: HttpRequestAssertionImpl

class QueryAssertionTest : FunSpec({

    beforeEach {
        underTest = HttpRequestAssertionImpl()
    }

    parameterWithoutValueTests()
    parameterStringTests()
    parameterIntTests()
    parameterLongTests()
    parameterDoubleTests()
    parametersTests()
    missingParametersTests()
    noParametersTests()

    test("parameters not asserted by default") {
        underTest.parameters.shouldBeNull()
    }

    test("parameters not asserted when just empty query assertion block is called") {
        underTest.url {
            query {}
        }

        underTest.parameters.shouldBeNull()
    }

    test("parameters assertions can be called together except noParameters") {
        underTest.url {
            query {
                shouldNotThrow<IllegalStateException> {
                    parameterWithoutValue("key1")
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
})

private fun FunSpec.parameterWithoutValueTests() {
    context("parameterWithoutValue") {
        test("set null value when called") {
            val key = "key"

            underTest.url {
                query {
                    parameterWithoutValue(key)
                }
            }

            underTest.parameters shouldBe mapOf(key to listOf(null))
        }

        test("add null value to the existing values of the same key") {
            val key = "key"
            val value = "value"
            val expected = mapOf(key to listOf(value, null))

            underTest.url {
                query {
                    parameter(key, value)
                    parameterWithoutValue(key)
                }
            }

            underTest.parameters shouldBe expected
        }

        noParametersExclusivityTest { parameterWithoutValue("key") }
    }
}

private fun FunSpec.parameterStringTests() {
    context("parameter (String)") {
        parameterTestSuite(
            createValueFrom = { it.toString() },
            callParameter = { key, value -> parameter(key, value) },
        )
    }
}

private fun FunSpec.parameterIntTests() {
    context("parameter (Int)") {
        parameterTestSuite(
            createValueFrom = { it },
            callParameter = { key, value -> parameter(key, value) },
        )
    }
}

private fun FunSpec.parameterLongTests() {
    context("parameter (Long)") {
        parameterTestSuite(
            createValueFrom = { it.toLong() },
            callParameter = { key, value -> parameter(key, value) },
        )
    }
}

private fun FunSpec.parameterDoubleTests() {
    context("parameter (Double)") {
        parameterTestSuite(
            createValueFrom = { it.toDouble() },
            callParameter = { key, value -> parameter(key, value) },
        )
    }
}

private fun FunSpec.parametersTests() {
    context("parameters") {
        parameterTestSuite(
            createValueFrom = { it.toString() },
            callParameter = { key, value -> parameters(key, listOf(value)) },
        )

        test("set multiple values") {
            val key = "key"
            val values = listOf("value1", "value2")
            val expected = mapOf(key to values)

            underTest.url {
                query {
                    parameters(key, values)
                }
            }

            underTest.parameters shouldBe expected
        }

        test("fail on empty list") {
            shouldThrow<IllegalArgumentException> {
                underTest.url {
                    query {
                        parameters("key", emptyList())
                    }
                }
            }
        }
    }
}

private fun FunSpec.missingParametersTests() {
    context("missingParameters") {
        test("not asserted by default") {
            underTest.missingParameters.shouldBeNull()
        }

        test("not asserted when just empty query assertion block is called") {
            underTest.url {
                query {}
            }

            underTest.missingParameters.shouldBeNull()
        }

        test("set missing parameters") {
            val firstKeys = setOf("key1", "key2")
            val secondKeys = setOf("key3", "key4")
            val expectedKeys = firstKeys + secondKeys

            underTest.url {
                query {
                    missingParameters(*firstKeys.toTypedArray())
                    missingParameters(*secondKeys.toTypedArray())
                }
            }

            underTest.missingParameters shouldBe expectedKeys
        }

        noParametersExclusivityTest { missingParameters("key") }
    }
}

private fun FunSpec.noParametersTests() {
    context("noParameters") {
        test("set no parameters") {
            underTest.url {
                query {
                    noParameters()
                }
            }

            underTest.parameters
                .shouldNotBeNull()
                .shouldBeEmpty()
        }
    }
}

private suspend fun <T> FunSpecContainerScope.parameterTestSuite(
    createValueFrom: (Int) -> T,
    callParameter: QueryAssertion.(String, T) -> Unit,
) {
    test("set value") {
        val key = "key"
        val value = createValueFrom(1)
        val expected = mapOf(key to listOf(value.toString()))

        underTest.url {
            query {
                callParameter(key, value)
            }
        }

        underTest.parameters shouldBe expected
    }

    test("set multiple params with different keys") {
        val key1 = "key1"
        val key2 = "key2"
        val value1 = createValueFrom(1)
        val value2 = createValueFrom(2)
        val expected = mapOf(
            key1 to listOf(value1.toString()),
            key2 to listOf(value2.toString()),
        )

        underTest.url {
            query {
                callParameter(key1, value1)
                callParameter(key2, value2)
            }
        }

        underTest.parameters shouldBe expected
    }

    test("add value to the existing values of the same key") {
        val key = "key"
        val value1 = createValueFrom(1)
        val value2 = createValueFrom(2)
        val expected = mapOf(key to listOf(value1.toString(), value2.toString()))

        underTest.url {
            query {
                callParameter(key, value1)
                callParameter(key, value2)
            }
        }

        underTest.parameters shouldBe expected
    }

    noParametersExclusivityTest { callParameter("key", createValueFrom(1)) }
}

private suspend fun FunSpecContainerScope.noParametersExclusivityTest(
    callParamsAssertion: QueryAssertion.() -> Unit,
) {
    test("noParameters can't be called after other params assertions") {
        underTest.url {
            query {
                callParamsAssertion()
                shouldThrow<IllegalStateException> { noParameters() }
            }
        }
    }

    test("other params assertions can't be called after noParameters") {
        underTest.url {
            query {
                noParameters()
                shouldThrow<IllegalStateException> { callParamsAssertion() }
            }
        }
    }
}

private val HttpRequestAssertionImpl.expectedQuery
    get() = expectedUrl.query

private val HttpRequestAssertionImpl.parameters
    get() = expectedQuery.parameters

private val HttpRequestAssertionImpl.missingParameters
    get() = expectedQuery.missingParameters
