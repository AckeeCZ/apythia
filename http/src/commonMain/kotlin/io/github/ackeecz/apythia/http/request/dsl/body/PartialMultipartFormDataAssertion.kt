package io.github.ackeecz.apythia.http.request.dsl.body

import io.github.ackeecz.apythia.http.ExperimentalHttpApi
import io.github.ackeecz.apythia.http.extension.DslExtensionConfigProvider
import io.github.ackeecz.apythia.http.request.ActualRequest
import io.github.ackeecz.apythia.http.request.body.ExpectedFormDataPart
import io.github.ackeecz.apythia.http.request.dsl.HttpRequestDslMarker

/**
 * Provides various methods for partial multipart/form-data assertions.
 */
@HttpRequestDslMarker
@ExperimentalHttpApi
public interface PartialMultipartFormDataAssertion : MultipartFormDataAssertion {

    /**
     * Asserts missing multipart/form-data parts. This is meant to be used mainly for parts with
     * unique [name]s or all parts with the given [name] have to be missing for the assertion to pass.
     *
     * @param name The name of the part specified in Content-Disposition header name parameter.
     */
    public fun missingParts(vararg name: String)
}

internal class PartialMultipartFormDataAssertionImpl(
    configProvider: DslExtensionConfigProvider,
    actualRequest: ActualRequest,
) : PartialMultipartFormDataAssertion {

    private var _missingParts: MutableSet<String>? = null
    val missingParts: Set<String>? get() = _missingParts?.toSet()

    private val multipartFormDataAssertion = MultipartFormDataAssertionImpl(configProvider, actualRequest)

    val expectedParts: List<ExpectedFormDataPart>?
        get() = multipartFormDataAssertion.expectedParts.ifEmpty { null }

    override fun part(
        name: String,
        filename: String?,
        assertPart: FormDataPartAssertion.() -> Unit,
    ) {
        multipartFormDataAssertion.part(name, filename, assertPart)
    }

    override fun missingParts(vararg name: String) {
        if (_missingParts == null) {
            _missingParts = mutableSetOf()
        }
        checkNotNull(_missingParts).addAll(name)
    }
}
