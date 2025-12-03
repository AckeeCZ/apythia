package io.github.ackeecz.apythia.http.request

import io.github.ackeecz.apythia.http.request.body.ExpectedBody
import io.github.ackeecz.apythia.http.request.header.ExpectedHeaders
import io.github.ackeecz.apythia.http.request.url.ExpectedUrl

/**
 * Represents an expected HTTP request used in assertions. `null` value means that the
 * corresponding part is not asserted.
 */
internal data class ExpectedRequest(
    val method: HttpMethod?,
    val url: ExpectedUrl,
    val headers: Headers,
    val body: Body,
) {

    constructor() : this(
        method = null,
        url = ExpectedUrl(),
        headers = Headers(),
        body = Body(),
    )

    data class Headers(
        val headers: ExpectedHeaders,
    ) {

        constructor() : this(
            headers = ExpectedHeaders(),
        )
    }

    internal data class Body(
        val body: ExpectedBody?,
    ) {

        constructor() : this(
            body = null,
        )
    }
}
