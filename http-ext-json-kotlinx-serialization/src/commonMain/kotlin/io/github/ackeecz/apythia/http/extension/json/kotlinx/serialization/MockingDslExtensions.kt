package io.github.ackeecz.apythia.http.extension.json.kotlinx.serialization

import io.github.ackeecz.apythia.http.ExperimentalHttpApi
import io.github.ackeecz.apythia.http.extension.getDslExtensionConfig
import io.github.ackeecz.apythia.http.response.dsl.HttpResponseMockBuilder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArrayBuilder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject

/**
 * Sets the [value] as JSON response body. [value] is encoded using the configured [Json] instance
 * if provided by [kotlinxSerializationJsonConfig]. Otherwise, the default [Json] instance is used.
 *
 * Content type header is set to "application/json" by default. If you want to omit this header
 * set [includeContentTypeHeader] to false.
 */
@ExperimentalHttpApi
public fun HttpResponseMockBuilder.jsonBody(
    value: JsonElement,
    includeContentTypeHeader: Boolean = true,
) {
    val config = getDslExtensionConfig<KotlinxSerializationJsonConfig>()
    val json = config?.mockingJson ?: Json
    jsonBody(
        value = json.encodeToString(value),
        includeContentTypeHeader = includeContentTypeHeader,
    )
}

/**
 * Same as [jsonBody] accepting [JsonElement] but takes a JSON string as input.
 */
@ExperimentalHttpApi
public fun HttpResponseMockBuilder.jsonBody(
    value: String,
    includeContentTypeHeader: Boolean = true,
) {
    bytesBody(
        value = value.encodeToByteArray(),
        contentType = if (includeContentTypeHeader) "application/json" else null
    )
}

/**
 * Same as [jsonBody] but uses a builder block to create a JSON object.
 */
@ExperimentalHttpApi
public fun HttpResponseMockBuilder.jsonObjectBody(
    includeContentTypeHeader: Boolean = true,
    build: JsonObjectBuilder.() -> Unit,
) {
    jsonBody(
        value = buildJsonObject(build),
        includeContentTypeHeader = includeContentTypeHeader,
    )
}

/**
 * Same as [jsonBody] but uses a builder block to create a JSON array.
 */
@ExperimentalHttpApi
public fun HttpResponseMockBuilder.jsonArrayBody(
    includeContentTypeHeader: Boolean = true,
    build: JsonArrayBuilder.() -> Unit,
) {
    jsonBody(
        value = buildJsonArray(build),
        includeContentTypeHeader = includeContentTypeHeader,
    )
}
