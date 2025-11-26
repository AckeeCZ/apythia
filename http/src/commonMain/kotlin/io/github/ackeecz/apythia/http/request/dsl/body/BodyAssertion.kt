package io.github.ackeecz.apythia.http.request.dsl.body

import io.github.ackeecz.apythia.http.Charset
import io.github.ackeecz.apythia.http.ExperimentalHttpApi
import io.github.ackeecz.apythia.http.extension.DslExtensionConfigProvider
import io.github.ackeecz.apythia.http.request.ActualHttpMessage
import io.github.ackeecz.apythia.http.request.body.ActualPart
import io.github.ackeecz.apythia.http.request.dsl.HttpRequestDslMarker
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe

/**
 * Provides various methods for HTTP body assertions.
 */
@HttpRequestDslMarker
@ExperimentalHttpApi
public interface BodyAssertion : DslExtensionConfigProvider {

    /**
     * The actual HTTP body. This can be used to extend the [BodyAssertion] DSL with
     * custom assertions. You can retrieve this value just once the same as you can call body
     * assertions just once.
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
    public suspend fun multipartFormData(assertMultipart: suspend MultipartFormDataAssertion.() -> Unit)

    /**
     * Allows to assert multipart/form-data body partially, i.e. you can assert only a specific
     * selected part of the multipart body, ignoring the rest without a failure unlike [multipartFormData].
     */
    public suspend fun partialMultipartFormData(assertPartialMultipart: suspend PartialMultipartFormDataAssertion.() -> Unit)
}

internal class BodyAssertionImpl(
    private val configProvider: DslExtensionConfigProvider,
    private val actualMessage: ActualHttpMessage,
    private val collectMultipartParts: suspend (ActualHttpMessage) -> List<ActualPart>,
) : BodyAssertion, DslExtensionConfigProvider by configProvider {

    override val actualBody = ActualBody(
        data = actualMessage.body,
        contentType = actualMessage.contentType,
    )

    override fun empty() {
        actualMessage.body.shouldBeEmpty()
    }

    override fun bytes(value: ByteArray) {
        actualMessage.body shouldBe value
    }

    @Suppress("DEPRECATION_ERROR")
    override fun plainText(value: String) {
        Charset.checkCharsetSupported(contentType = actualMessage.contentType)
        actualMessage.body.decodeToString() shouldBeEqual value
    }

    override fun plainText(value: Int) {
        plainText(value.toString())
    }

    override fun plainText(value: Double) {
        plainText(value.toString())
    }

    override suspend fun multipartFormData(assertMultipart: suspend MultipartFormDataAssertion.() -> Unit) {
        val assertion = MultipartFormDataAssertionImpl(
            configProvider = configProvider,
            collectParts = collectMultipartParts,
            actualMessage = actualMessage,
        ).apply { assertMultipart() }
        val actualParts = assertion.remainingActualParts
        withClue("multipart/form-data body contains unexpected parts: $actualParts") {
            actualParts.shouldBeEmpty()
        }
    }

    override suspend fun partialMultipartFormData(assertPartialMultipart: suspend PartialMultipartFormDataAssertion.() -> Unit) {
        PartialMultipartFormDataAssertionImpl(
            configProvider = configProvider,
            collectParts = collectMultipartParts,
            actualMessage = actualMessage,
        ).assertPartialMultipart()
    }
}
