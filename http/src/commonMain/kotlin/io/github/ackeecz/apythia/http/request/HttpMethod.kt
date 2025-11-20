package io.github.ackeecz.apythia.http.request

/**
 * Abstraction over HTTP method.
 */
public enum class HttpMethod(internal val value: String) {

    GET("GET"),
    PUT("PUT"),
    POST("POST"),
    DELETE("DELETE"),
}
