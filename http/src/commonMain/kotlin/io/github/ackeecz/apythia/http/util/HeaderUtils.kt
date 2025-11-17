package io.github.ackeecz.apythia.http.util

import kotlin.random.Random

internal val Map<String, List<String>>.contentType
    get() = lowercaseKeys()["content-type"]?.firstOrNull()

internal val Map<String, List<String>>.containsContentType
    get() = contentType != null

internal fun Map<String, List<String>>.lowercaseKeys(): Map<String, List<String>> {
    return mapKeys { it.key.lowercase() }
}

internal fun String.randomCase(): String {
    return this.map { char ->
        if (Random.nextBoolean()) char.uppercaseChar() else char.lowercaseChar()
    }.joinToString("")
}
