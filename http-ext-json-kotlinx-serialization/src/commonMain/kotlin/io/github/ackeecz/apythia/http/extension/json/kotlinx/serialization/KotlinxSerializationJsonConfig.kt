package io.github.ackeecz.apythia.http.extension.json.kotlinx.serialization

import io.github.ackeecz.apythia.http.extension.DslExtensionConfig
import io.github.ackeecz.apythia.http.extension.DslExtensionConfigs
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonBuilder

internal class KotlinxSerializationJsonConfig(
    val assertionJson: Json,
    val arrangementJson: Json,
) : DslExtensionConfig

/**
 * Same as [kotlinxSerializationJsonConfig] overload but uses the same [Json] instance for all
 * encoding/decoding operations.
 */
public fun DslExtensionConfigs.kotlinxSerializationJsonConfig(
    buildJson: JsonBuilder.() -> Unit,
) {
    kotlinxSerializationJsonConfig(Json { buildJson() })
}

/**
 * Same as [kotlinxSerializationJsonConfig] overload but uses the same [Json] instance for all
 * encoding/decoding operations.
 */
public fun DslExtensionConfigs.kotlinxSerializationJsonConfig(json: Json) {
    kotlinxSerializationJsonConfig(json, json)
}

/**
 * Configures the JSON encoder and decoder used by [jsonBody] and other JSON-related DSL extensions.
 * For more information about configurations, see [DslExtensionConfig]'s documentation.
 *
 * @param assertionJson JSON configuration used for encoding/decoding JSONs during assertions.
 * @param arrangementJson JSON configuration used for encoding/decoding JSONs during arrangements.
 */
public fun DslExtensionConfigs.kotlinxSerializationJsonConfig(
    assertionJson: Json,
    arrangementJson: Json,
) {
    val config = KotlinxSerializationJsonConfig(
        assertionJson = assertionJson,
        arrangementJson = arrangementJson,
    )
    dslExtensionConfig(config)
}
