package io.github.ackeecz.apythia.http.request

/**
 * Abstraction over HTTP method.
 */
public enum class HttpMethod(internal val value: String) {

    CONNECT("CONNECT"),
    DELETE("DELETE"),
    GET("GET"),
    HEAD("HEAD"),
    OPTIONS("OPTIONS"),
    PATCH("PATCH"),
    POST("POST"),
    PUT("PUT"),
    TRACE("TRACE"),
}
