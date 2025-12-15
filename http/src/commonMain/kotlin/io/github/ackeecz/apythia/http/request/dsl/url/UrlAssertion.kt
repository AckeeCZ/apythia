package io.github.ackeecz.apythia.http.request.dsl.url

import com.eygraber.uri.Url
import com.eygraber.uri.decodeUri
import io.github.ackeecz.apythia.http.ExperimentalHttpApi
import io.github.ackeecz.apythia.http.extension.DslExtensionConfigProvider
import io.github.ackeecz.apythia.http.request.dsl.HttpRequestDslMarker
import io.github.ackeecz.apythia.http.util.CallCountChecker
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldEndWith

/**
 * Provides various methods for HTTP request URL assertions.
 */
@HttpRequestDslMarker
@ExperimentalHttpApi
public interface UrlAssertion : DslExtensionConfigProvider {

    /**
     * The actual decoded URL of a HTTP request. This can be used to extend the [UrlAssertion] DSL with
     * custom assertions.
     */
    public val actualUrl: String

    /**
     * Asserts full decoded request [url].
     */
    public fun url(url: String)

    /**
     * Asserts full decoded request URL [path].
     */
    public fun path(path: String)

    /**
     * Asserts decoded request URL [pathSuffix].
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

    private val queryCallCountChecker = CallCountChecker(actionName = "query")

    override val actualUrl = actualTypedUrl.toString().decodeUri()

    override fun url(url: String) {
        actualUrl shouldBe url
    }

    override fun path(path: String) {
        actualTypedUrl.path shouldBe path
    }

    override fun pathSuffix(pathSuffix: String) {
        actualTypedUrl.path shouldEndWith pathSuffix
    }

    override fun query(assertQuery: QueryAssertion.() -> Unit) {
        queryCallCountChecker.incrementOrFail()
        QueryAssertionImpl(
            configProvider = configProvider,
            actualUrl = actualTypedUrl,
        ).assertQuery()
    }
}
