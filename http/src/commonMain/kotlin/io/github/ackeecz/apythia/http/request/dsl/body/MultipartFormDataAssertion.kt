package io.github.ackeecz.apythia.http.request.dsl.body

import io.github.ackeecz.apythia.http.ExperimentalHttpApi
import io.github.ackeecz.apythia.http.extension.DslExtensionConfigProvider
import io.github.ackeecz.apythia.http.request.ActualHttpMessage
import io.github.ackeecz.apythia.http.request.body.ActualFormDataPart
import io.github.ackeecz.apythia.http.request.body.ActualPart
import io.github.ackeecz.apythia.http.request.dsl.HttpRequestDslMarker
import io.github.ackeecz.apythia.http.util.header.getContentDispositionHeader
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe

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
    public suspend fun part(
        name: String,
        filename: String? = null,
        assertPart: suspend FormDataPartAssertion.() -> Unit,
    )
}

internal class MultipartFormDataAssertionImpl private constructor(
    private val configProvider: DslExtensionConfigProvider,
    private val collectNestedParts: suspend (ActualHttpMessage) -> List<ActualPart>,
    actualParts: List<ActualPart>,
) : MultipartFormDataAssertion {

    private val _remainingActualParts = actualParts.toFormDataParts().toMutableList()

    /**
     * Remaining actual multipart/form-data parts that have not been matched yet by calling [part].
     * Returned list is an immutable snapshot of the current state.
     */
    val remainingActualParts: List<ActualFormDataPart> get() = _remainingActualParts.toList()

    private fun List<ActualPart>.toFormDataParts(): List<ActualFormDataPart> = map { part ->
        val contentDispositionHeader = getContentDispositionHeader(part.headers.toMap())
        val partName = checkNotNull(contentDispositionHeader?.name) {
            "multipart/form-data part is missing Content-Disposition header with 'name' parameter"
        }
        ActualFormDataPart(
            name = partName,
            filename = contentDispositionHeader.filename,
            message = ActualHttpMessage(part.headers, part.body),
        )
    }

    override suspend fun part(
        name: String,
        filename: String?,
        assertPart: suspend FormDataPartAssertion.() -> Unit,
    ) {
        val matchingParts = remainingActualParts.filter { it.name == name }
        withClue("Expected part with name '$name' and filename '$filename' not found") {
            matchingParts.shouldNotBeEmpty()
        }
        for (index in matchingParts.indices) {
            val actualPart = matchingParts[index]
            try {
                val actualFormDataName = actualPart.name
                withClue("Assertion failed for form data part with name '$actualFormDataName'") {
                    actualFormDataName shouldBe name
                    actualPart.filename shouldBe filename
                    FormDataPartAssertionImpl(configProvider, actualPart, collectNestedParts).assertPart()
                }
                _remainingActualParts.remove(actualPart)
                break
            } catch (e: AssertionError) {
                if (index == matchingParts.lastIndex) {
                    throw e
                }
            }
        }
    }

    companion object {

        suspend operator fun invoke(
            configProvider: DslExtensionConfigProvider,
            collectParts: suspend (ActualHttpMessage) -> List<ActualPart>,
            actualMessage: ActualHttpMessage,
        ): MultipartFormDataAssertionImpl {
            val actualParts = collectParts(actualMessage)
            return MultipartFormDataAssertionImpl(
                configProvider = configProvider,
                collectNestedParts = collectParts,
                actualParts = actualParts,
            )
        }
    }
}
