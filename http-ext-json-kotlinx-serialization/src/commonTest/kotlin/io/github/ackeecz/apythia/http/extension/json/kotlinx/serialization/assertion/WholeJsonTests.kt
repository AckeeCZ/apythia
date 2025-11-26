package io.github.ackeecz.apythia.http.extension.json.kotlinx.serialization.assertion

import io.github.ackeecz.apythia.http.UnsupportedEncodingException
import io.github.ackeecz.apythia.http.extension.DslExtensionConfigs
import io.github.ackeecz.apythia.http.extension.json.kotlinx.serialization.json
import io.github.ackeecz.apythia.http.extension.json.kotlinx.serialization.jsonElement
import io.github.ackeecz.apythia.http.extension.json.kotlinx.serialization.jsonObject
import io.github.ackeecz.apythia.http.extension.json.kotlinx.serialization.kotlinxSerializationJsonConfig
import io.github.ackeecz.apythia.http.request.dsl.body.BodyAssertion
import io.github.ackeecz.apythia.testing.http.HttpApythiaMock
import io.github.ackeecz.apythia.testing.http.shouldFail
import io.github.ackeecz.apythia.testing.http.shouldNotFail
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.spec.style.scopes.FunSpecContainerScope
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

internal fun FunSpec.wholeJsonTests(fixture: AssertionDslExtensionsTest.Fixture) = with(fixture) {
    context("whole JSON body") {
        jsonStringTests(fixture)
        jsonElementTests(fixture)
        jsonObjectTests(fixture)
    }
}

private suspend fun FunSpecContainerScope.jsonStringTests(
    fixture: AssertionDslExtensionsTest.Fixture,
) = with(fixture) {
    context("JSON string") {
        commonWholeJsonTestSuite(
            fixture = fixture,
            assertJson = { jsonObject, json ->
                json(json.encodeToString(jsonObject))
            }
        )
    }
}

private suspend fun FunSpecContainerScope.jsonElementTests(
    fixture: AssertionDslExtensionsTest.Fixture,
) = with(fixture) {
    context("JSON element") {
        commonWholeJsonTestSuite(
            fixture = fixture,
            assertJson = { jsonObject, _ -> jsonElement(jsonObject) }
        )
    }
}

private suspend fun FunSpecContainerScope.jsonObjectTests(
    fixture: AssertionDslExtensionsTest.Fixture,
) = with(fixture) {
    context("JSON object") {
        commonWholeJsonTestSuite(
            fixture = fixture,
            assertJson = { jsonObject, _ ->
                jsonObject {
                    jsonObject.entries.forEach { (key, value) ->
                        put(key, value)
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalSerializationApi::class)
private suspend fun FunSpecContainerScope.commonWholeJsonTestSuite(
    fixture: AssertionDslExtensionsTest.Fixture,
    assertJson: BodyAssertion.(JsonObject, Json) -> Unit,
) = with(fixture) {
    test("failure") {
        val underTest = createSut()
        val unexpectedJson = buildJsonObject { put("key", "unexpected") }
        underTest.setActualRequestBody(unexpectedJson)

        shouldFail {
            underTest.assertNextRequest {
                body {
                    assertJson(buildJsonObject {}, defaultJson)
                }
            }
        }
    }

    test("success") {
        val jsonObject = buildJsonObject { put("key", "value") }
        val underTest = createSut()
        underTest.setActualRequestBody(jsonObject)

        shouldNotFail {
            underTest.assertNextRequest {
                body {
                    assertJson(jsonObject, defaultJson)
                }
            }
        }
    }

    test("fail when unsupported encoding is specified in header") {
        val underTest = createSut()
        underTest.actualRequestHeaders = mapOf("Content-Type" to listOf("application/json; charset=utf-16"))

        shouldThrow<UnsupportedEncodingException> {
            underTest.assertNextRequest {
                body {
                    assertJson(buildJsonObject {}, defaultJson)
                }
            }
        }
    }

    test("succeed when supported UTF-8 encoding is specified in header") {
        val jsonObject = buildJsonObject { put("key", "value") }
        val underTest = createSut()
        underTest.setActualRequestBody(jsonObject)
        underTest.actualRequestHeaders = mapOf("Content-Type" to listOf("application/json; charset=utf-8"))

        shouldNotFail {
            underTest.assertNextRequest {
                body {
                    assertJson(jsonObject, defaultJson)
                }
            }
        }
    }

    test("succeed when supported UTF-8 encoding is used but no encoding is specified in header") {
        val jsonObject = buildJsonObject { put("key", "value") }
        val underTest = createSut()
        underTest.setActualRequestBody(jsonObject)
        underTest.actualRequestHeaders = mapOf("Content-Type" to listOf("application/json"))

        shouldNotFail {
            underTest.assertNextRequest {
                body {
                    assertJson(jsonObject, defaultJson)
                }
            }
        }
    }

    context("config") {
        suspend fun testAddedConfig(addConfig: DslExtensionConfigs.(Json) -> Unit) {
            val json = Json { allowTrailingComma = true }
            val underTest = HttpApythiaMock { addConfig(json) }
            val actualJsonObject = """{"key":"value",}"""
            underTest.actualRequestBody = actualJsonObject.encodeToByteArray()

            underTest.assertNextRequest {
                body {
                    assertJson(json.decodeFromString(actualJsonObject), json)
                }
            }
        }

        test("use default JSON config if not specified") {
            shouldThrow<SerializationException> {
                testAddedConfig(addConfig = {})
            }
        }

        test("use added JSON config if specified by single JSON") {
            shouldNotThrow<SerializationException> {
                testAddedConfig { kotlinxSerializationJsonConfig(it) }
            }
        }

        test("use added JSON config if specified by particular assertion JSON") {
            shouldNotThrow<SerializationException> {
                testAddedConfig { json ->
                    kotlinxSerializationJsonConfig(assertionJson = json, arrangementJson = Json)
                }
            }
        }
    }
}
