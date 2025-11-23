package io.github.ackeecz.apythia.http.request

import com.eygraber.uri.Url
import io.github.ackeecz.apythia.http.ExperimentalHttpApi

/**
 * Actual HTTP request that was sent by the HTTP client. This data is used for assertions.
 */
@ExperimentalHttpApi
public class ActualRequest(
    internal val method: String,
    url: String,
    headers: Map<String, List<String>>,
    body: ByteArray,
) {

    internal val url: Url = Url.parse(url)

    internal val message = ActualHttpMessage(
        headers = headers,
        body = body,
    )

    internal fun copy(
        method: String = this.method,
        url: String = this.url.toString(),
        headers: Map<String, List<String>> = this.message.headers,
        body: ByteArray = this.message.body,
    ): ActualRequest {
        return ActualRequest(
            method = method,
            url = url,
            headers = headers,
            body = body,
        )
    }
}
