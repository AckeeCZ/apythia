package io.github.ackeecz.apythia.http.request.header

/**
 * Represents expected HTTP headers. `null` value means that the corresponding part is not asserted.
 */
internal data class ExpectedHeaders(
    val headers: Map<String, List<String>>?,
) {

    constructor() : this(
        headers = null,
    )
}
