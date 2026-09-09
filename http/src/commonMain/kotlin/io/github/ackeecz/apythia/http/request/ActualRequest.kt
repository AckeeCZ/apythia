package io.github.ackeecz.apythia.http.request

import com.eygraber.uri.Url
import io.github.ackeecz.apythia.http.ExperimentalHttpApi
import io.github.ackeecz.apythia.http.util.url.getDecodedQueryParameters

/**
 * Actual HTTP request that was sent by the HTTP client. This data is used for assertions.
 *
 * @param method HTTP method of the request.
 * @param url Encoded URL of the request. Both `+` and `%20` space encoding is supported in the query.
 * @param headers Headers of the request.
 * @param body Raw byte body of the request.
 */
@ExperimentalHttpApi
public class ActualRequest(
    public val method: String,
    url: String,
    public val headers: Map<String, List<String>>,
    public val body: ByteArray,
) {

    public val urlString: String = url
    internal val url: Url

    init {
        val parsedUrl = Url.parse(url)
        // We need to replace + with %20 in the query to be able to properly decode spaces encoded as +,
        // because Url decodes only %20 to spaces.
        val encodedQuery = parsedUrl.encodedQuery?.replace("+", "%20")
        this.url = parsedUrl.buildUpon()
            .encodedQuery(encodedQuery)
            .build()
            .toString()
            .let(Url::parse)
    }

    /**
     * Decoded URL path of the request, e.g. "/api/v2/video/detail".
     */
    @ExperimentalHttpApi
    public val path: String get() = url.path.orEmpty()

    /**
     * Decoded query parameters of the request grouped by a parameter name in order of appearance.
     * `null` value means that the query parameter has no value, e.g. "name" or "name=". `+` in the
     * query is decoded to a space.
     */
    @ExperimentalHttpApi
    public val queryParameters: Map<String, List<String?>>
        get() = url.getDecodedQueryParameters()

    /**
     * First decoded value of the query parameter with the given [name]. `null` if the parameter is
     * missing or is present without a value.
     */
    @ExperimentalHttpApi
    public fun queryParameter(name: String): String? = queryParameters[name]?.firstOrNull()

    internal val message = ActualHttpMessage(headers = headers, body = body)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as ActualRequest

        if (method != other.method) return false
        if (message != other.message) return false
        if (url != other.url) return false

        return true
    }

    override fun hashCode(): Int {
        var result = method.hashCode()
        result = 31 * result + message.hashCode()
        result = 31 * result + url.hashCode()
        return result
    }

    override fun toString(): String {
        return "ActualRequest(method=$method, message=$message, url=$url)"
    }
}
