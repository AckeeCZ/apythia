package io.github.ackeecz.apythia.http.extension.json.kotlinx.serialization

import io.github.ackeecz.apythia.http.extension.DslExtensionConfigs
import io.github.ackeecz.apythia.http.response.HttpResponse
import io.github.ackeecz.apythia.http.response.dsl.HttpResponseMockBuilder
import io.github.ackeecz.apythia.testing.http.HttpApythiaMock
import io.github.ackeecz.apythia.testing.http.contentType
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.spec.style.scopes.FunSpecContainerScope
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

private const val JSON_CONTENT_TYPE_VALUE = "application/json"

class MockingDslExtensionsTest : FunSpec({

    jsonElementBodyTests()
    jsonStringBodyTests()
    jsonObjectBuilderBodyTests()
    jsonArrayBuilderBodyTests()
})

private fun createSut(json: Json? = null): HttpApythiaMock {
    return HttpApythiaMock {
        kotlinxSerializationJsonConfig(json ?: Json)
    }
}

private fun FunSpec.jsonElementBodyTests() {
    context("JSON element") {
        commonTestSuite(
            setBody = { value, includeContentTypeHeader ->
                jsonBody(value, includeContentTypeHeader)
            },
            setBodyWithDefaultHeader = {
                jsonBody(it)
            },
        )

        configTestSuite(
            setBody = { value ->
                jsonBody(buildJsonArray { add(value) })
            },
            getActual = { json ->
                requireResponse().body
                    .decodeToObject<List<Double>>(json)
                    .first()
            },
        )
    }
}

private fun FunSpec.jsonStringBodyTests() {
    context("JSON string") {
        val json = Json

        commonTestSuite(
            setBody = { value, includeContentTypeHeader ->
                jsonBody(json.encodeToString(value), includeContentTypeHeader)
            },
            setBodyWithDefaultHeader = {
                jsonBody(json.encodeToString(it))
            },
        )
    }
}

private fun FunSpec.jsonObjectBuilderBodyTests() {
    context("JSON object builder") {
        commonTestSuite(
            setBody = { value, includeContentTypeHeader ->
                jsonObjectBody(includeContentTypeHeader) {
                    value.entries.forEach { (key, value) -> put(key, value) }
                }
            },
            setBodyWithDefaultHeader = { value ->
                jsonObjectBody {
                    value.entries.forEach { (key, value) -> put(key, value) }
                }
            },
        )

        configTestSuite(
            setBody = { value ->
                jsonObjectBody { put("key", value) }
            },
            getActual = { json ->
                requireResponse().body
                    .decodeToObject<JsonObject>(json)["key"]
                    .shouldNotBeNull()
                    .jsonPrimitive
                    .double
            },
        )
    }
}

private fun FunSpec.jsonArrayBuilderBodyTests() {
    context("JSON array builder") {
        commonTestSuite(
            setBody = { value, includeContentTypeHeader ->
                jsonArrayBody(includeContentTypeHeader) { add(value) }
            },
            setBodyWithDefaultHeader = { jsonArrayBody { add(it) } },
            getExpected = { buildJsonArray { add(it) } },
        )

        configTestSuite(
            setBody = { value ->
                jsonArrayBody { add(value) }
            },
            getActual = { json ->
                requireResponse().body
                    .decodeToObject<List<Double>>(json)
                    .first()
            },
        )
    }
}

private suspend fun FunSpecContainerScope.commonTestSuite(
    setBody: HttpResponseMockBuilder.(JsonObject, Boolean) -> Unit,
    setBodyWithDefaultHeader: HttpResponseMockBuilder.(JsonObject) -> Unit,
    getExpected: (JsonObject) -> JsonElement = { it },
) {
    test("can be called only once") {
        createSut().mockNextResponse {
            setBodyWithDefaultHeader(buildJsonObject {})

            shouldThrow<IllegalStateException> {
                setBodyWithDefaultHeader(buildJsonObject {})
            }
        }
    }

    test("set value") {
        val value = buildJsonObject {
            put("key", "value")
        }
        val underTest = createSut()

        underTest.mockNextResponse {
            setBodyWithDefaultHeader(value)
        }

        underTest.requireResponse().body
            .decodeToObject<JsonElement>()
            .shouldBe(getExpected(value))
    }

    test("by default include content type header") {
        val underTest = createSut()

        underTest.mockNextResponse {
            setBodyWithDefaultHeader(buildJsonObject {})
        }

        underTest.requireResponse().headers.contentType shouldBe JSON_CONTENT_TYPE_VALUE
    }

    test("include content type header if true") {
        val underTest = createSut()

        underTest.mockNextResponse {
            setBody(buildJsonObject {}, true)
        }

        underTest.requireResponse().headers.contentType shouldBe JSON_CONTENT_TYPE_VALUE
    }

    test("do not include content type header if false") {
        val underTest = createSut()

        underTest.mockNextResponse {
            setBody(buildJsonObject {}, false)
        }

        underTest.requireResponse().headers.contentType shouldBe null
    }

    test("throw exception when content type header already present") {
        createSut().mockNextResponse {
            headers {
                header("cONtEnt-TYpE", JSON_CONTENT_TYPE_VALUE)
            }

            shouldThrow<IllegalStateException> {
                setBody(buildJsonObject {}, true)
            }
        }
    }
}

private suspend fun FunSpecContainerScope.configTestSuite(
    setBody: HttpResponseMockBuilder.(Double) -> Unit,
    getActual: HttpApythiaMock.(Json) -> Double,
) {
    context("config") {
        test("use default JSON config if not specified") {
            shouldThrow<SerializationException> {
                createSut(json = null).mockNextResponse {
                    setBody(Double.NaN)
                }
            }
        }

        fun testAddedConfig(addConfig: DslExtensionConfigs.(Json) -> Unit) {
            val json = Json { allowSpecialFloatingPointValues = true }
            val underTest = HttpApythiaMock { addConfig(json) }

            underTest.mockNextResponse { setBody(Double.NaN) }

            underTest.getActual(json).isNaN().shouldBeTrue()
        }

        test("use added JSON config if specified by single JSON") {
            testAddedConfig { kotlinxSerializationJsonConfig(it) }
        }

        test("use added JSON config if specified by particular mocking JSON") {
            testAddedConfig { json ->
                kotlinxSerializationJsonConfig(assertionJson = Json, mockingJson = json)
            }
        }

        test("use added JSON config if specified by JSON builder") {
            testAddedConfig { json ->
                kotlinxSerializationJsonConfig {
                    allowSpecialFloatingPointValues = json.configuration.allowSpecialFloatingPointValues
                }
            }
        }
    }
}

private inline fun <reified T> ByteArray.decodeToObject(json: Json = Json): T {
    return json.decodeFromString(decodeToString())
}

private fun HttpApythiaMock.requireResponse(): HttpResponse {
    return actualResponse.shouldNotBeNull()
}
