package io.github.ackeecz.apythia.http.request.body

import io.github.ackeecz.apythia.http.request.header.ExpectedHeaders

internal data class ExpectedFormDataPart(
    val name: String,
    val filename: String?,
    val headers: Headers,
    val body: Body,
) {

    data class Headers(
        val headers: ExpectedHeaders,
    ) {

        constructor() : this(
            headers = ExpectedHeaders(),
        )
    }

    data class Body(
        val body: ExpectedBody?,
    ) {

        constructor() : this(
            body = null,
        )
    }
}
