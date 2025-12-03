package io.github.ackeecz.apythia.http.request.dsl.url

import io.github.ackeecz.apythia.http.ExperimentalHttpApi
import io.github.ackeecz.apythia.http.request.dsl.HttpRequestDsl
import io.github.ackeecz.apythia.http.request.url.ExpectedUrl
import io.github.ackeecz.apythia.http.util.CallCountChecker

/**
 * Provides various methods for HTTP request URL assertions.
 */
@HttpRequestDsl
@ExperimentalHttpApi
public interface UrlAssertion {

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

internal class UrlAssertionImpl : UrlAssertion {

    var expectedUrl = ExpectedUrl()

    private val urlCallCountChecker = CallCountChecker(actionName = "url")
    private val pathCallCountChecker = CallCountChecker(actionName = "path")
    private val pathSuffixCallCountChecker = CallCountChecker(actionName = "pathSuffix")
    private val queryCallCountChecker = CallCountChecker(actionName = "query")

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
        val queryAssertion = QueryAssertionImpl().apply(assertQuery)
        expectedUrl = expectedUrl.copy(query = queryAssertion.expectedQuery)
    }
}
