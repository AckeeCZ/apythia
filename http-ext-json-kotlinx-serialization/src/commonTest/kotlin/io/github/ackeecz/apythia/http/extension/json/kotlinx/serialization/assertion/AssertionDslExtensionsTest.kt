package io.github.ackeecz.apythia.http.extension.json.kotlinx.serialization.assertion

import io.github.ackeecz.apythia.http.extension.json.kotlinx.serialization.kotlinxSerializationJsonConfig
import io.github.ackeecz.apythia.testing.http.HttpApythiaMock
import io.kotest.core.spec.style.FunSpec
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

class AssertionDslExtensionsTest : FunSpec({

    val fixture = Fixture()

    wholeJsonTests(fixture)
    partialJsonObjectTests(fixture)
}) {

    class Fixture {

        val defaultJson = Json

        fun createSut(json: Json? = null): HttpApythiaMock {
            return HttpApythiaMock {
                kotlinxSerializationJsonConfig(json ?: Json)
            }
        }

        fun HttpApythiaMock.setActualRequestBody(json: JsonElement) {
            actualRequestBody = Json.encodeToString(json).encodeToByteArray()
        }
    }
}
