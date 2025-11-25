package io.github.ackeecz.apythia.http.request

import com.eygraber.uri.Url

/**
 * This is a generalization of [ActualRequest]. Difference is that [ActualRequest] represents
 * request data only while [RequestTargetWithMessage] represents request [method] + request [url]
 * (request target) combined with [message] that can come from either a HTTP request or multipart part.
 */
internal data class RequestTargetWithMessage(
    val method: String,
    val url: Url,
    val message: ActualHttpMessage,
)
