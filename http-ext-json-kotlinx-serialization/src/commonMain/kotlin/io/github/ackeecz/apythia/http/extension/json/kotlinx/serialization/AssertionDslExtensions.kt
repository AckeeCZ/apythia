package io.github.ackeecz.apythia.http.extension.json.kotlinx.serialization

import io.github.ackeecz.apythia.http.Charset
import io.github.ackeecz.apythia.http.ExperimentalHttpApi
import io.github.ackeecz.apythia.http.extension.getDslExtensionConfig
import io.github.ackeecz.apythia.http.request.dsl.body.BodyAssertion
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject

/**
 * Asserts whole JSON body [value] as String.
 */
@ExperimentalHttpApi
public fun BodyAssertion.json(value: String) {
    jsonElement(getJson().decodeFromString(value))
}

@ExperimentalHttpApi
private fun BodyAssertion.getJson(): Json {
    return getDslExtensionConfig<KotlinxSerializationJsonConfig>()?.assertionJson ?: Json
}

/**
 * Asserts whole JSON body [value] as [JsonElement].
 */
@ExperimentalHttpApi
public fun BodyAssertion.jsonElement(value: JsonElement) {
    getBodyJsonElement() shouldBe value
}

@Suppress("DEPRECATION_ERROR")
private fun BodyAssertion.getBodyJsonElement(): JsonElement {
    val actualBody = actualBody
    Charset.checkCharsetSupported(actualBody.contentType)
    return actualBody.data.decodeToString().let {
        getJson().decodeFromString<JsonElement>(it)
    }
}

/**
 * Asserts whole JSON body as [JsonObject] built by the given [build] block.
 */
@ExperimentalHttpApi
public fun BodyAssertion.jsonObject(build: JsonObjectBuilder.() -> Unit) {
    jsonElement(buildJsonObject(build))
}

/**
 * Allows to assert only a part of the JSON object as specified by [PartialJsonObjectAssertion]
 */
@ExperimentalHttpApi
public fun BodyAssertion.partialJsonObject(assertPartialJsonObject: PartialJsonObjectAssertion.() -> Unit) {
    val jsonObject = getBodyJsonElement().jsonObject
    PartialJsonObjectAssertionImpl(jsonObject).apply(assertPartialJsonObject)
}
