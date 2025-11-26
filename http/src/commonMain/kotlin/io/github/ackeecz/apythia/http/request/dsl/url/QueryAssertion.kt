package io.github.ackeecz.apythia.http.request.dsl.url

import com.eygraber.uri.Url
import io.github.ackeecz.apythia.http.ExperimentalHttpApi
import io.github.ackeecz.apythia.http.extension.DslExtensionConfigProvider
import io.github.ackeecz.apythia.http.request.dsl.HttpRequestDslMarker
import io.kotest.assertions.withClue
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.nulls.shouldNotBeNull

/**
 * Provides various methods for HTTP request query assertions.
 */
@HttpRequestDslMarker
@ExperimentalHttpApi
public interface QueryAssertion : DslExtensionConfigProvider {

    /**
     * The actual URL query parameters of a HTTP request. Each key is a query parameter name and
     * the value is a list of query parameter values, e.g. "name=value1&name=value2". `null` value
     * means that the query parameter has no value, e.g. "name" or "name=".
     * This can be used to extend the [QueryAssertion] DSL with custom assertions.
     */
    public val actualQueryParameters: Map<String, List<String?>>

    /**
     * Asserts that a query parameter with the given [name] has no value,
     * e.g. "www.example.com/path?foo".
     */
    public fun noValueParameter(name: String)

    /**
     * Asserts a query parameter with the given [name] and [value].
     */
    public fun parameter(name: String, value: String)

    /**
     * Same as [parameter] String [value] overload but for Int [value].
     */
    public fun parameter(name: String, value: Int)

    /**
     * Same as [parameter] String [value] overload but for Long [value].
     */
    public fun parameter(name: String, value: Long)

    /**
     * Same as [parameter] String [value] overload but for Double [value].
     */
    public fun parameter(name: String, value: Double)

    /**
     * Asserts that each value from [values] is a separate query parameter with the same [name].
     * `null` value means that the query parameter has no value, e.g. "name" or "name=".
     *
     * @param values Values must not be empty
     */
    public fun parameters(name: String, values: List<String?>)

    /**
     * Asserts that query parameters with the given [name] are missing.
     */
    public fun missingParameters(vararg name: String)

    /**
     * Asserts that no query parameters are present.
     */
    public fun noParameters()
}

internal class QueryAssertionImpl(
    configProvider: DslExtensionConfigProvider,
    actualUrl: Url,
) : QueryAssertion, DslExtensionConfigProvider by configProvider {

    override val actualQueryParameters = actualUrl.getQueryParameters()

    private fun Url.getQueryParameters(): Map<String, List<String?>> {
        return getQueryParameterNames().associateWith { name ->
            getQueryParameters(name).map {
                it.ifEmpty { null }
            }
        }
    }

    override fun noValueParameter(name: String) {
        val actualValues = assertCommonProperties(name)
        withClue("Parameter '$name' has a value") {
            actualValues.shouldContain(null)
        }
    }

    private fun assertCommonProperties(name: String): List<String?> {
        return assertParameterIsNotMissing(name)
    }

    private fun assertParameterIsNotMissing(name: String): List<String?> {
        return withClue("Parameter '$name' is missing") {
            actualQueryParameters[name].shouldNotBeNull()
        }
    }

    override fun parameter(name: String, value: String) {
        val actualValues = assertCommonProperties(name)
        actualValues shouldContain value
    }

    override fun parameter(name: String, value: Int) {
        parameter(name, value.toString())
    }

    override fun parameter(name: String, value: Long) {
        parameter(name, value.toString())
    }

    override fun parameter(name: String, value: Double) {
        parameter(name, value.toString())
    }

    override fun parameters(name: String, values: List<String?>) {
        require(values.isNotEmpty()) { "values must not be empty" }
        val actualValues = assertParameterIsNotMissing(name)
        actualValues shouldContainAll values
    }

    override fun missingParameters(vararg name: String) {
        val actualParamNames = actualQueryParameters.keys
        name.forEach { expectedParam ->
            withClue("Query parameter '$expectedParam' should be missing but is present.") {
                actualParamNames.contains(expectedParam).shouldBeFalse()
            }
        }
    }

    override fun noParameters() {
        val paramNames = actualQueryParameters.keys
        withClue("Expected no query parameters but were: $paramNames") {
            paramNames.shouldBeEmpty()
        }
    }
}
