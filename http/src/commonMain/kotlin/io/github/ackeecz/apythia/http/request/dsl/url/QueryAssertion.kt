package io.github.ackeecz.apythia.http.request.dsl.url

import com.eygraber.uri.Url
import io.github.ackeecz.apythia.http.ExperimentalHttpApi
import io.github.ackeecz.apythia.http.extension.DslExtensionConfigProvider
import io.github.ackeecz.apythia.http.request.dsl.HttpRequestDslMarker
import io.github.ackeecz.apythia.http.request.url.ExpectedQuery
import io.github.ackeecz.apythia.http.util.MutualExclusivityChecker

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
    public fun parameterWithoutValue(name: String)

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
     *
     * @param values Values must not be empty
     */
    public fun parameters(name: String, values: List<String>)

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

    var expectedQuery = ExpectedQuery()
        private set

    private var parameters: Map<String, List<String?>>?
        get() = expectedQuery.parameters
        set(value) {
            expectedQuery = expectedQuery.copy(parameters = value)
        }

    private var missingParameters: Set<String>?
        get() = expectedQuery.missingParameters
        set(value) {
            expectedQuery = expectedQuery.copy(missingParameters = value)
        }

    private val paramsAssertionExclusivityChecker = MutualExclusivityChecker<ParamsAssertionGroup>()

    override val actualQueryParameters = actualUrl.getQueryParameters()

    private fun Url.getQueryParameters(): Map<String, List<String?>> {
        return getQueryParameterNames().associateWith { name ->
            getQueryParameters(name).map {
                it.ifEmpty { null }
            }
        }
    }

    override fun parameterWithoutValue(name: String) {
        parametersInternal(name, listOf(null))
    }

    private fun parametersInternal(name: String, values: List<String?>) {
        paramsAssertionExclusivityChecker.checkGroup(ParamsAssertionGroup.Other)
        if (parameters == null) {
            parameters = emptyMap()
        }
        val currentParams = checkNotNull(parameters).toMutableMap()
        currentParams[name] = currentParams.getOrElse(name) { emptyList() } + values
        parameters = currentParams
    }

    override fun parameter(name: String, value: String) {
        parametersInternal(name, listOf(value))
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

    override fun parameters(name: String, values: List<String>) {
        require(values.isNotEmpty()) { "values must not be empty" }
        parametersInternal(name, values)
    }

    override fun missingParameters(vararg name: String) {
        paramsAssertionExclusivityChecker.checkGroup(ParamsAssertionGroup.Other)
        if (missingParameters == null) {
            missingParameters = emptySet()
        }
        val allMissingParams = checkNotNull(missingParameters)
            .toMutableSet()
            .apply { addAll(name) }
        missingParameters = allMissingParams
    }

    override fun noParameters() {
        paramsAssertionExclusivityChecker.checkGroup(ParamsAssertionGroup.NoParameters)
        parameters = emptyMap()
    }

    private enum class ParamsAssertionGroup(override val groupName: String) : MutualExclusivityChecker.Group {

        NoParameters("noParameters"),
        Other("other params assertions"),
    }
}
