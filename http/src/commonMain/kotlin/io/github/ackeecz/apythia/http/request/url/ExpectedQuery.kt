package io.github.ackeecz.apythia.http.request.url

/**
 * Represents an expected query used in assertions. `null` value means that the
 * corresponding part is not asserted.
 */
internal data class ExpectedQuery(
    val parameters: Map<String, List<String?>>?,
    val missingParameters: Set<String>?,
) {
    constructor() : this(
        parameters = null,
        missingParameters = null,
    )
}
