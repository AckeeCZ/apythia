package io.github.ackeecz.apythia.http.response.dsl

/**
 * DSL for building mocked HTTP response headers.
 */
@HttpResponseDslMarker
public interface HeadersMockBuilder {

    /**
     * Sets a single header with the specified [name] and [value].
     */
    public fun header(name: String, value: String)

    /**
     * Sets multiple headers with the specified [name] and [values], i.e. there will be a header
     * entry per each value with the same [name].
     */
    public fun headers(name: String, values: List<String>)

    /**
     * Sets multiple headers from the specified [headers] map.
     */
    public fun headers(headers: Map<String, String>)
}

internal class HeadersMockBuilderImpl : HeadersMockBuilder {

    private val _headers: MutableMap<String, List<String>> = mutableMapOf()
    val headers: Map<String, List<String>> get() = _headers.toMap()

    override fun header(name: String, value: String) {
        headers(name, listOf(value))
    }

    override fun headers(name: String, values: List<String>) {
        val existingValues = _headers.getOrElse(name) { emptyList() }
        _headers[name] = existingValues + values
    }

    override fun headers(headers: Map<String, String>) {
        headers.forEach { (name, value) -> header(name, value) }
    }
}
