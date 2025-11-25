package io.github.ackeecz.apythia.http.request.dsl.body

import io.github.ackeecz.apythia.http.ExperimentalHttpApi
import io.github.ackeecz.apythia.http.extension.DslExtensionConfigProvider
import io.github.ackeecz.apythia.http.extension.HttpDslExtension
import io.github.ackeecz.apythia.http.request.body.ExpectedBody
import io.github.ackeecz.apythia.http.request.dsl.HttpRequestDslMarker
import io.github.ackeecz.apythia.http.util.CallCountChecker

/**
 * Provides various methods for HTTP body assertions.
 */
@HttpRequestDslMarker
@ExperimentalHttpApi
public interface BodyAssertion : DslExtensionConfigProvider {

    /**
     * Asserts an empty body.
     */
    public fun empty()

    /**
     * Asserts a raw bytes body [value].
     */
    public fun bytes(value: ByteArray)

    /**
     * Asserts a plain text body [value].
     */
    public fun plainText(value: String)

    /**
     * Same as [plainText] String overload but for Int [value].
     */
    public fun plainText(value: Int)

    /**
     * Same as [plainText] String overload but for Double [value].
     */
    public fun plainText(value: Double)

    /**
     * Asserts whole multipart/form-data body. Expected multipart parts have to match the actual ones,
     * i.e. their count has to be the same and each actual part has to match the expected one.
     */
    public fun multipartFormData(assertMultipart: MultipartFormDataAssertion.() -> Unit)

    /**
     * Allows to assert multipart/form-data body partially, i.e. you can assert only a specific
     * selected part of the multipart body, ignoring the rest without a failure unlike [multipartFormData].
     */
    public fun partialMultipartFormData(assertPartialMultipart: PartialMultipartFormDataAssertion.() -> Unit)

    /**
     * Asserts a body using the specified [extension]. This API is meant to be used for custom
     * extensions that extend the [BodyAssertion] DSL by adding more content types to check, e.g.
     * JSON, XML, etc. It has the same semantic as other [BodyAssertion] methods like [empty], [bytes],
     * etc, so you should use it **only** for checking particular body data using a custom content type
     * and not for anything else.
     */
    public fun dslExtension(extension: HttpDslExtension)
}

internal class BodyAssertionImpl(
    private val configProvider: DslExtensionConfigProvider,
) : BodyAssertion, DslExtensionConfigProvider by configProvider {

    var expectedBody: ExpectedBody? = null
        private set

    private val contentTypeCallCountChecker = CallCountChecker(actionName = "content type assertion")

    override fun empty() {
        contentTypeCallCountChecker.incrementOrFail()
        expectedBody = ExpectedBody.Empty
    }

    override fun bytes(value: ByteArray) {
        contentTypeCallCountChecker.incrementOrFail()
        expectedBody = ExpectedBody.Bytes(value)
    }

    override fun plainText(value: String) {
        contentTypeCallCountChecker.incrementOrFail()
        expectedBody = ExpectedBody.PlainText(value)
    }

    override fun plainText(value: Int) {
        plainText(value.toString())
    }

    override fun plainText(value: Double) {
        plainText(value.toString())
    }

    override fun multipartFormData(assertMultipart: MultipartFormDataAssertion.() -> Unit) {
        contentTypeCallCountChecker.incrementOrFail()
        val assertion = MultipartFormDataAssertionImpl(configProvider).apply(assertMultipart)
        val parts = assertion.expectedParts
        check(parts.isNotEmpty()) { "multipart/form-data must have at least one part" }
        expectedBody = ExpectedBody.MultipartFormData(parts)
    }

    override fun partialMultipartFormData(assertPartialMultipart: PartialMultipartFormDataAssertion.() -> Unit) {
        contentTypeCallCountChecker.incrementOrFail()
        val assertion = PartialMultipartFormDataAssertionImpl(configProvider).apply(assertPartialMultipart)
        expectedBody = ExpectedBody.PartialMultipartFormData(
            parts = assertion.expectedParts,
            missingParts = assertion.missingParts,
        )
    }

    override fun dslExtension(extension: HttpDslExtension) {
        contentTypeCallCountChecker.incrementOrFail()
        expectedBody = ExpectedBody.DslExtension(extension)
    }
}
