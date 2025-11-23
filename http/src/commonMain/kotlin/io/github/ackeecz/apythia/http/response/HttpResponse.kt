package io.github.ackeecz.apythia.http.response

import io.github.ackeecz.apythia.http.ExperimentalHttpApi
import io.github.ackeecz.apythia.http.HttpApythia
import io.github.ackeecz.apythia.http.util.ByteArrayWrapper
import io.github.ackeecz.apythia.http.util.wrap

/**
 * Http response data to be arranged by [HttpApythia].
 */
@ExperimentalHttpApi
public class HttpResponse internal constructor(
    public val statusCode: Int,
    public val headers: Map<String, List<String>>,
    private val wrappedBody: ByteArrayWrapper,
) {

    public val body: ByteArray get() = wrappedBody.byteArray

    internal constructor(
        statusCode: Int,
        headers: Map<String, List<String>>,
        body: ByteArray,
    ) : this(
        statusCode = statusCode,
        headers = headers,
        wrappedBody = body.wrap(),
    )

    internal fun copy(
        statusCode: Int = this.statusCode,
        headers: Map<String, List<String>> = this.headers,
        body: ByteArray = this.body,
    ): HttpResponse {
        return HttpResponse(statusCode, headers, body)
    }
}
