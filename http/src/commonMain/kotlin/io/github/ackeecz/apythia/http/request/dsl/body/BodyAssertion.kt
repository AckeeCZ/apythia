package io.github.ackeecz.apythia.http.request.dsl.body

import io.github.ackeecz.apythia.http.ExperimentalHttpApi
import io.github.ackeecz.apythia.http.extension.DslExtensionConfigProvider
import io.github.ackeecz.apythia.http.request.ActualRequest
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
     * The actual body of a HTTP request. This can be used to extend the [BodyAssertion] DSL with
     * custom assertions.
     */
    public val actualBody: ActualBody

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
}

internal class BodyAssertionImpl(
    private val configProvider: DslExtensionConfigProvider,
    private val actualRequest: ActualRequest,
) : BodyAssertion, DslExtensionConfigProvider by configProvider {

    var expectedBody: ExpectedBody? = null
        private set

    private val contentTypeCallCountChecker = CallCountChecker(actionName = "content type assertion")

    override val actualBody: ActualBody
        get() {
            contentTypeCallCountChecker.incrementOrFail()
            return ActualBody(
                data = actualRequest.message.body,
                contentType = actualRequest.message.contentType,
            )
        }

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
        val assertion = MultipartFormDataAssertionImpl(configProvider, actualRequest).apply(assertMultipart)
        val parts = assertion.expectedParts
        check(parts.isNotEmpty()) { "multipart/form-data must have at least one part" }
        expectedBody = ExpectedBody.MultipartFormData(parts)
    }

    override fun partialMultipartFormData(assertPartialMultipart: PartialMultipartFormDataAssertion.() -> Unit) {
        contentTypeCallCountChecker.incrementOrFail()
        val assertion = PartialMultipartFormDataAssertionImpl(configProvider, actualRequest).apply(assertPartialMultipart)
        expectedBody = ExpectedBody.PartialMultipartFormData(
            parts = assertion.expectedParts,
            missingParts = assertion.missingParts,
        )
    }
}
