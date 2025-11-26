package io.github.ackeecz.apythia.testing.http

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow

public val Map<String, List<String>>.contentType: String?
    get() = lowercaseKeys()["content-type"]?.firstOrNull()

public val Map<String, List<String>>.containsContentType: Boolean
    get() = contentType != null

public fun Map<String, List<String>>.lowercaseKeys(): Map<String, List<String>> {
    return mapKeys { it.key.lowercase() }
}

public inline fun shouldNotFail(block: () -> Unit) {
    shouldNotThrow<AssertionError> { block() }
}

public inline fun shouldFail(block: () -> Unit) {
    shouldThrow<AssertionError> { block() }
}
