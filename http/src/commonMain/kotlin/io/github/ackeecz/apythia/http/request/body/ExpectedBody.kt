package io.github.ackeecz.apythia.http.request.body

import io.github.ackeecz.apythia.http.util.ByteArrayWrapper
import io.github.ackeecz.apythia.http.util.wrap

internal sealed interface ExpectedBody {

    data object Empty : ExpectedBody

    data class Bytes(private val wrappedValue: ByteArrayWrapper) : ExpectedBody {

        val value: ByteArray get() = wrappedValue.byteArray

        constructor(value: ByteArray) : this(value.wrap())
    }

    data class PlainText(val value: String) : ExpectedBody

    data class MultipartFormData(val parts: List<ExpectedFormDataPart>) : ExpectedBody

    /**
     * Same as [MultipartFormData] but allows to assert only a specific selected part(s) of the multipart body.
     */
    data class PartialMultipartFormData(
        val parts: List<ExpectedFormDataPart>?,
        val missingParts: Set<String>? = null,
    ) : ExpectedBody
}
