package io.github.ackeecz.apythia.http.request.dsl.body

import io.github.ackeecz.apythia.http.ExperimentalHttpApi
import io.github.ackeecz.apythia.http.extension.DslExtensionConfigProvider
import io.github.ackeecz.apythia.http.request.ActualRequest
import io.github.ackeecz.apythia.http.request.body.ExpectedFormDataPart
import io.github.ackeecz.apythia.http.request.dsl.HttpRequestDslMarker

/**
 * Provides various methods for multipart/form-data assertions.
 */
@HttpRequestDslMarker
@ExperimentalHttpApi
public interface MultipartFormDataAssertion {

    /**
     * Asserts a multipart/form-data part.
     *
     * @param name The name of the part specified in Content-Disposition header name parameter.
     * @param filename Optional filename of the part specified in Content-Disposition header filename parameter.
     * @param assertPart Asserts additional properties of the part.
     */
    public fun part(
        name: String,
        filename: String? = null,
        assertPart: FormDataPartAssertion.() -> Unit,
    )
}

internal class MultipartFormDataAssertionImpl(
    private val configProvider: DslExtensionConfigProvider,
    private val actualRequest: ActualRequest,
) : MultipartFormDataAssertion {

    private val _expectedParts: MutableList<ExpectedFormDataPart> = mutableListOf()
    val expectedParts: List<ExpectedFormDataPart> get() = _expectedParts.toList()

    override fun part(
        name: String,
        filename: String?,
        assertPart: FormDataPartAssertion.() -> Unit,
    ) {
        val partAssertion = FormDataPartAssertionImpl(configProvider, actualRequest).apply(assertPart)
        val part = ExpectedFormDataPart(
            name = name,
            filename = filename,
            headers = partAssertion.expectedHeaders,
            body = partAssertion.expectedBody,
        )
        _expectedParts.add(part)
    }
}
