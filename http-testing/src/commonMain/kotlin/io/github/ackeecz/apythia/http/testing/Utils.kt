package io.github.ackeecz.apythia.http.testing

public fun Map<String, List<String>>.lowercaseKeys(): Map<String, List<String>> {
    return mapKeys { it.key.lowercase() }
}
