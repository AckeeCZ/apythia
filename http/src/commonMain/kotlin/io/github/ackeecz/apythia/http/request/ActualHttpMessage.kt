package io.github.ackeecz.apythia.http.request

import io.github.ackeecz.apythia.http.ExperimentalHttpApi
import io.github.ackeecz.apythia.http.util.header.contentType

/**
 * Groups together [headers] and [body]. This can represent headers and body of either a HTTP
 * request or a multipart part.
 */
@ExperimentalHttpApi
public class ActualHttpMessage(
    public val headers: Map<String, List<String>>,
    public val body: ByteArray,
) {

    public val contentType: String? get() = headers.contentType
    public val contentLength: Int get() = body.size

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as ActualHttpMessage

        if (headers != other.headers) return false
        if (!body.contentEquals(other.body)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = headers.hashCode()
        result = 31 * result + body.contentHashCode()
        return result
    }

    override fun toString(): String {
        return "ActualHttpMessage(headers=$headers, body=${body.toHexString()})"
    }
}
