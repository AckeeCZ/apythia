package io.github.ackeecz.apythia.http.request.dsl.header

import io.github.ackeecz.apythia.http.ExperimentalHttpApi
import io.github.ackeecz.apythia.http.extension.DslExtensionConfigProvider
import io.github.ackeecz.apythia.http.request.dsl.HttpRequestDslMarker
import io.github.ackeecz.apythia.http.util.CallCountChecker
import io.github.ackeecz.apythia.http.util.header.Headers
import io.github.ackeecz.apythia.http.util.header.appendHeaderParameters
import io.github.ackeecz.apythia.http.util.header.lowercaseKeys
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.nulls.shouldNotBeNull

/**
 * Provides various methods for HTTP headers assertions.
 */
@HttpRequestDslMarker
@ExperimentalHttpApi
public interface HeadersAssertion : DslExtensionConfigProvider {

    /**
     * The actual headers of the request or multipart part. This can be used to extend the
     * [HeadersAssertion] DSL with custom assertions.
     */
    public val actualHeaders: Map<String, List<String>>

    /**
     * Asserts a [value] for a header with the given [name].
     */
    public fun header(name: String, value: String)

    /**
     * Asserts multiple [values] for a header with the given [name]. This can be either a comma-separated
     * list of values of a single header or multiple headers with the same name.
     *
     * @param name The name of the header.
     * @param values The values of the header. Values must not be empty.
     */
    public fun headers(name: String, values: List<String>)

    /**
     * Asserts a Content-Type header with the given [mimeType] and optional [parameters].
     * [parameters] has to be ordered the same way as they appear in the actual header.
     */
    public fun contentType(mimeType: String, parameters: Map<String, String> = emptyMap())
}

internal class HeadersAssertionImpl(
    private val configProvider: DslExtensionConfigProvider,
    override val actualHeaders: Map<String, List<String>>,
) : HeadersAssertion, DslExtensionConfigProvider by configProvider {

    private val contentTypeCallCountChecker = CallCountChecker(actionName = "Content-Type")

    override fun header(name: String, value: String) {
        assertCommonProperties(name) shouldContain value
    }

    private fun assertCommonProperties(name: String): List<String> {
        return assertHeaderIsNotMissing(name)
    }

    private fun assertHeaderIsNotMissing(name: String): List<String> {
        return withClue("Header '$name' is missing") {
            actualHeaders.lowercaseKeys()[name.lowercase()].shouldNotBeNull()
        }
    }

    override fun headers(name: String, values: List<String>) {
        require(values.isNotEmpty()) { "values must not be empty" }
        assertCommonProperties(name) shouldContainAll values
    }

    override fun contentType(mimeType: String, parameters: Map<String, String>) {
        contentTypeCallCountChecker.incrementOrFail()
        header(Headers.CONTENT_TYPE, mimeType.appendHeaderParameters(parameters))
    }
}
