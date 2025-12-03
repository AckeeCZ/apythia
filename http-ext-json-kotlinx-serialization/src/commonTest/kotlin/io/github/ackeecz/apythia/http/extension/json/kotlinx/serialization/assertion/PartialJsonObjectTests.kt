package io.github.ackeecz.apythia.http.extension.json.kotlinx.serialization.assertion

import io.github.ackeecz.apythia.http.extension.json.kotlinx.serialization.PartialJsonObjectAssertion
import io.github.ackeecz.apythia.http.extension.json.kotlinx.serialization.partialJsonObject
import io.github.ackeecz.apythia.testing.http.shouldFail
import io.github.ackeecz.apythia.testing.http.shouldNotFail
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.spec.style.scopes.FunSpecContainerScope
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

internal fun FunSpec.partialJsonObjectTests(
    fixture: AssertionDslExtensionsTest.Fixture
) = with(fixture) {
    context("partial JSON object") {
        propertyTest(fixture)
        missingPropertyTests(fixture)
    }
}

private suspend fun FunSpecContainerScope.propertyTest(
    fixture: AssertionDslExtensionsTest.Fixture,
) = with(fixture) {
    context("property") {
        nullPropertyTests(fixture)
        propertyStringTests(fixture)
        propertyIntTests(fixture)
        propertyLongTests(fixture)
        propertyFloatTests(fixture)
        propertyDoubleTests(fixture)
        propertyBooleanTests(fixture)
    }
}

@OptIn(ExperimentalSerializationApi::class)
private suspend fun FunSpecContainerScope.nullPropertyTests(
    fixture: AssertionDslExtensionsTest.Fixture,
) = with(fixture) {
    context("null") {
        propertyNotFoundTest(
            fixture = fixture,
            callProperty = { key, _ -> nullProperty(key) },
            transformToSupportedValue = { null },
        )

        test("failure when value is unexpected") {
            val underTest = fixture.createSut()
            val expectedKey = "key"
            val jsonObject = buildJsonObject { put(expectedKey, 1) }
            underTest.setActualRequestBody(jsonObject)

            shouldFail {
                underTest.assertNextRequest {
                    body {
                        partialJsonObject {
                            nullProperty(expectedKey)
                        }
                    }
                }
            }
        }

        test("success") {
            val underTest = fixture.createSut()
            val expectedKey = "key"
            val jsonObject = buildJsonObject { put(expectedKey, null) }
            underTest.setActualRequestBody(jsonObject)

            shouldNotFail {
                underTest.assertNextRequest {
                    body {
                        partialJsonObject {
                            nullProperty(expectedKey)
                        }
                    }
                }
            }
        }
    }
}

private suspend fun FunSpecContainerScope.propertyStringTests(
    fixture: AssertionDslExtensionsTest.Fixture,
) = with(fixture) {
    context("string") {
        commonPropertyTestSuite(
            fixture = fixture,
            callProperty = { key, value -> property(key, value) },
            transformToSupportedValue = { it.toString() },
            putValue = { key, value -> put(key, value.toString()) },
        )

        test("failure when value is not String") {
            val underTest = fixture.createSut()
            val expectedKey = "key"
            val actualValue = 1
            val jsonObject = buildJsonObject { put(expectedKey, actualValue) }
            underTest.setActualRequestBody(jsonObject)

            shouldFail {
                underTest.assertNextRequest {
                    body {
                        partialJsonObject {
                            property(expectedKey, actualValue.toString())
                        }
                    }
                }
            }
        }
    }
}

private suspend fun FunSpecContainerScope.propertyIntTests(
    fixture: AssertionDslExtensionsTest.Fixture,
) = with(fixture) {
    context("int") {
        commonPropertyTestSuite(
            fixture = fixture,
            callProperty = { key, value -> property(key, value) },
            transformToSupportedValue = { it },
            putValue = { key, value -> put(key, value) },
        )

        test("failure when value is not Int") {
            val underTest = fixture.createSut()
            val expectedKey = "key"
            val actualValue = 1
            val jsonObject = buildJsonObject { put(expectedKey, actualValue.toString()) }
            underTest.setActualRequestBody(jsonObject)

            shouldFail {
                underTest.assertNextRequest {
                    body {
                        partialJsonObject {
                            property(expectedKey, actualValue)
                        }
                    }
                }
            }
        }
    }
}

private suspend fun FunSpecContainerScope.propertyLongTests(
    fixture: AssertionDslExtensionsTest.Fixture,
) = with(fixture) {
    context("long") {
        commonPropertyTestSuite(
            fixture = fixture,
            callProperty = { key, value -> property(key, value) },
            transformToSupportedValue = { it.toLong() },
            putValue = { key, value -> put(key, value.toLong()) },
        )

        test("failure when value is not Long") {
            val underTest = fixture.createSut()
            val expectedKey = "key"
            val actualValue = 1L
            val jsonObject = buildJsonObject { put(expectedKey, actualValue.toString()) }
            underTest.setActualRequestBody(jsonObject)

            shouldFail {
                underTest.assertNextRequest {
                    body {
                        partialJsonObject {
                            property(expectedKey, actualValue)
                        }
                    }
                }
            }
        }
    }
}

private suspend fun FunSpecContainerScope.propertyFloatTests(
    fixture: AssertionDslExtensionsTest.Fixture,
) = with(fixture) {
    context("float") {
        commonPropertyTestSuite(
            fixture = fixture,
            callProperty = { key, value -> property(key, value) },
            transformToSupportedValue = { it.toFloat() },
            putValue = { key, value -> put(key, value.toFloat()) },
        )

        test("failure when value is not Float") {
            val underTest = fixture.createSut()
            val expectedKey = "key"
            val actualValue = 1F
            val jsonObject = buildJsonObject { put(expectedKey, actualValue.toString()) }
            underTest.setActualRequestBody(jsonObject)

            shouldFail {
                underTest.assertNextRequest {
                    body {
                        partialJsonObject {
                            property(expectedKey, actualValue)
                        }
                    }
                }
            }
        }
    }
}

private suspend fun FunSpecContainerScope.propertyDoubleTests(
    fixture: AssertionDslExtensionsTest.Fixture,
) = with(fixture) {
    context("double") {
        commonPropertyTestSuite(
            fixture = fixture,
            callProperty = { key, value -> property(key, value) },
            transformToSupportedValue = { it.toDouble() },
            putValue = { key, value -> put(key, value.toDouble()) },
        )

        test("failure when value is not Double") {
            val underTest = fixture.createSut()
            val expectedKey = "key"
            val actualValue = 1.0
            val jsonObject = buildJsonObject { put(expectedKey, actualValue.toString()) }
            underTest.setActualRequestBody(jsonObject)

            shouldFail {
                underTest.assertNextRequest {
                    body {
                        partialJsonObject {
                            property(expectedKey, actualValue)
                        }
                    }
                }
            }
        }
    }
}

private suspend fun FunSpecContainerScope.propertyBooleanTests(
    fixture: AssertionDslExtensionsTest.Fixture,
) = with(fixture) {
    context("boolean") {
        fun Int.toBoolean(): Boolean = this != 0

        commonPropertyTestSuite(
            fixture = fixture,
            callProperty = { key, value -> property(key, value) },
            transformToSupportedValue = { it.toBoolean() },
            putValue = { key, value -> put(key, value.toBoolean()) },
        )

        test("failure when value is not Boolean") {
            val underTest = fixture.createSut()
            val expectedKey = "key"
            val actualValue = "true"
            val jsonObject = buildJsonObject { put(expectedKey, actualValue) }
            underTest.setActualRequestBody(jsonObject)

            shouldFail {
                underTest.assertNextRequest {
                    body {
                        partialJsonObject {
                            property(expectedKey, true)
                        }
                    }
                }
            }
        }
    }
}

private suspend fun <Value> FunSpecContainerScope.commonPropertyTestSuite(
    fixture: AssertionDslExtensionsTest.Fixture,
    callProperty: PartialJsonObjectAssertion.(String, Value) -> Unit,
    transformToSupportedValue: (Int) -> Value,
    putValue: JsonObjectBuilder.(String, Int) -> Unit,
) = with(fixture) {
    propertyNotFoundTest(fixture, callProperty, transformToSupportedValue)

    test("failure when value is unexpected") {
        val underTest = fixture.createSut()
        val expectedKey = "key"
        val actualValue = 0
        val expectedValue = actualValue + 1
        val jsonObject = buildJsonObject { putValue(expectedKey, actualValue) }
        underTest.setActualRequestBody(jsonObject)

        shouldFail {
            underTest.assertNextRequest {
                body {
                    partialJsonObject {
                        callProperty(expectedKey, transformToSupportedValue(expectedValue))
                    }
                }
            }
        }
    }

    test("success") {
        val underTest = fixture.createSut()
        val expectedKey = "key"
        val expectedValue = 1
        val jsonObject = buildJsonObject { putValue(expectedKey, expectedValue) }
        underTest.setActualRequestBody(jsonObject)

        shouldNotFail {
            underTest.assertNextRequest {
                body {
                    partialJsonObject {
                        callProperty(expectedKey, transformToSupportedValue(expectedValue))
                    }
                }
            }
        }
    }
}

private suspend fun <Value> FunSpecContainerScope.propertyNotFoundTest(
    fixture: AssertionDslExtensionsTest.Fixture,
    callProperty: PartialJsonObjectAssertion.(String, Value) -> Unit,
    transformToSupportedValue: (Int) -> Value,
) = with(fixture) {
    test("failure when property not found") {
        val underTest = fixture.createSut()
        val jsonObject = buildJsonObject { put("unexpectedKey", "") }
        underTest.setActualRequestBody(jsonObject)

        shouldFail {
            underTest.assertNextRequest {
                body {
                    partialJsonObject {
                        callProperty("expectedKey", transformToSupportedValue(0))
                    }
                }
            }
        }
    }
}

private suspend fun FunSpecContainerScope.missingPropertyTests(
    fixture: AssertionDslExtensionsTest.Fixture,
) = with(fixture) {
    context("missing property") {
        test("fail when properties are present") {
            val underTest = fixture.createSut()
            val key1 = "key1"
            val key2 = "key2"
            val jsonObject = buildJsonObject {
                put(key1, "")
                put(key2, "")
            }
            underTest.setActualRequestBody(jsonObject)

            shouldFail {
                underTest.assertNextRequest {
                    body {
                        partialJsonObject {
                            missingProperties(key1, key2)
                        }
                    }
                }
            }
        }

        test("succeed when properties are missing") {
            val underTest = fixture.createSut()
            val key1 = "key1"
            val key2 = "key2"
            val jsonObject = buildJsonObject {
                put("other property", "")
                put("another property", "")
            }
            underTest.setActualRequestBody(jsonObject)

            shouldNotFail {
                underTest.assertNextRequest {
                    body {
                        partialJsonObject {
                            missingProperties(key1, key2)
                        }
                    }
                }
            }
        }
    }
}
