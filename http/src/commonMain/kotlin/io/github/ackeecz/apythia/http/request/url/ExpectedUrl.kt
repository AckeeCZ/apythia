package io.github.ackeecz.apythia.http.request.url

/**
 * Represents an expected URL used in assertions. `null` value means that the
 * corresponding part is not asserted.
 */
internal data class ExpectedUrl(
    val url: String?,
    val path: String?,
    val pathSuffix: String?,
    val query: ExpectedQuery,
) {

    constructor() : this(
        url = null,
        path = null,
        pathSuffix = null,
        query = ExpectedQuery(),
    )
}
