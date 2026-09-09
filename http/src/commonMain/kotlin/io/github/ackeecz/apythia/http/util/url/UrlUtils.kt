package io.github.ackeecz.apythia.http.util.url

import com.eygraber.uri.Url

/**
 * Decoded query parameters of the URL grouped by a parameter name in order of appearance. `null`
 * value means that the query parameter has no value, e.g. "name" or "name=".
 */
internal fun Url.getDecodedQueryParameters(): Map<String, List<String?>> {
    return getQueryParameterNames().associateWith { name ->
        getQueryParameters(name).map { value -> value.ifEmpty { null } }
    }
}
