package io.github.ackeecz.apythia.http.util.header

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

internal fun String.appendHeaderParameters(parameters: Map<String, String>): String {
    return buildString {
        append(this@appendHeaderParameters)
        if (parameters.isNotEmpty()) {
            append("; ")
            append(parameters.entries.joinToString("; ") { "${it.key}=${it.value}" })
        }
    }
}

internal fun getContentDispositionHeader(headers: Map<String, List<String>>): ContentDispositionHeader? {
    val contentDisposition = headers.lowercaseKeys()["content-disposition"]?.firstOrNull() ?: return null
    fun extract(key: String): String? {
        // Matches both key="value" and key=value
        val regex = """$key="?([^";]+)"?""".toRegex()
        val match = regex.find(contentDisposition)
        return match?.groups?.get(1)?.value
    }
    return ContentDispositionHeader(
        name = checkNotNull(extract("name")) { "Content-Disposition header must contain a 'name' parameter" },
        filename = extract("filename"),
    )
}
