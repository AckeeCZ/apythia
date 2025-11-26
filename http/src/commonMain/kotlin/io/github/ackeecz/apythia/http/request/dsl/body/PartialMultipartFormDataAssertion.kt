package io.github.ackeecz.apythia.http.request.dsl.body

import io.github.ackeecz.apythia.http.ExperimentalHttpApi
import io.github.ackeecz.apythia.http.extension.DslExtensionConfigProvider
import io.github.ackeecz.apythia.http.request.ActualHttpMessage
import io.github.ackeecz.apythia.http.request.body.ActualPart
import io.github.ackeecz.apythia.http.request.dsl.HttpRequestDslMarker
import io.kotest.assertions.withClue
import io.kotest.matchers.booleans.shouldBeFalse

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

internal class PartialMultipartFormDataAssertionImpl private constructor(
    private val multipartFormDataAssertion: MultipartFormDataAssertionImpl,
) : PartialMultipartFormDataAssertion {

    private val allActualParts = multipartFormDataAssertion.remainingActualParts

    override suspend fun part(
        name: String,
        filename: String?,
        assertPart: suspend FormDataPartAssertion.() -> Unit,
    ) {
        multipartFormDataAssertion.part(name, filename, assertPart)
    }

    override fun missingParts(vararg name: String) {
        name.forEach { expectedMissingName ->
            withClue("Multipart parts '$expectedMissingName' should be missing but are present.") {
                allActualParts.map { it.name }.contains(expectedMissingName).shouldBeFalse()
            }
        }
    }

    companion object {

        suspend operator fun invoke(
            configProvider: DslExtensionConfigProvider,
            collectParts: suspend (ActualHttpMessage) -> List<ActualPart>,
            actualMessage: ActualHttpMessage,
        ): PartialMultipartFormDataAssertionImpl {
            val multipartFormDataAssertion = MultipartFormDataAssertionImpl(
                configProvider = configProvider,
                collectParts = collectParts,
                actualMessage = actualMessage,
            )
            return PartialMultipartFormDataAssertionImpl(
                multipartFormDataAssertion = multipartFormDataAssertion,
            )
        }
    }
}
