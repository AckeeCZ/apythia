package io.github.ackeecz.apythia.http.request.dsl.url

import com.eygraber.uri.Url
import io.github.ackeecz.apythia.http.ExperimentalHttpApi
import io.github.ackeecz.apythia.http.extension.DslExtensionConfigProvider
import io.github.ackeecz.apythia.http.request.dsl.HttpRequestDslMarker
import io.github.ackeecz.apythia.http.request.url.ExpectedUrl
import io.github.ackeecz.apythia.http.util.CallCountChecker

/**
 * Provides various methods for HTTP request URL assertions.
 */
@HttpRequestDslMarker
@ExperimentalHttpApi
public interface UrlAssertion : DslExtensionConfigProvider {

    /**
     * The actual URL of a HTTP request. This can be used to extend the [UrlAssertion] DSL with
     * custom assertions.
     */
    public val actualUrl: String

    /**
     * Asserts full request [url].
     */
    public fun url(url: String)

    /**
     * Asserts full request URL [path].
     */
    public fun path(path: String)

    /**
     * Asserts request URL [pathSuffix].
     */
    public fun pathSuffix(pathSuffix: String)

    /**
     * Asserts request URL [query].
     */
    public fun query(assertQuery: QueryAssertion.() -> Unit)
}

internal class UrlAssertionImpl(
    private val configProvider: DslExtensionConfigProvider,
    private val actualTypedUrl: Url,
) : UrlAssertion, DslExtensionConfigProvider by configProvider {

    var expectedUrl = ExpectedUrl()

    private val urlCallCountChecker = CallCountChecker(actionName = "url")
    private val pathCallCountChecker = CallCountChecker(actionName = "path")
    private val pathSuffixCallCountChecker = CallCountChecker(actionName = "pathSuffix")
    private val queryCallCountChecker = CallCountChecker(actionName = "query")

    override val actualUrl = actualTypedUrl.toString()

    override fun url(url: String) {
        urlCallCountChecker.incrementOrFail()
        expectedUrl = expectedUrl.copy(url = url)
    }

    override fun path(path: String) {
        pathCallCountChecker.incrementOrFail()
        expectedUrl = expectedUrl.copy(path = path)
    }

    override fun pathSuffix(pathSuffix: String) {
        pathSuffixCallCountChecker.incrementOrFail()
        expectedUrl = expectedUrl.copy(pathSuffix = pathSuffix)
    }

    override fun query(assertQuery: QueryAssertion.() -> Unit) {
        queryCallCountChecker.incrementOrFail()
        val queryAssertion = QueryAssertionImpl(
            configProvider = configProvider,
            actualUrl = actualTypedUrl,
        ).apply(assertQuery)
        expectedUrl = expectedUrl.copy(query = queryAssertion.expectedQuery)
    }
}
