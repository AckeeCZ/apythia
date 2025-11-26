package io.github.ackeecz.apythia.http.request.body

import io.github.ackeecz.apythia.http.request.ActualHttpMessage

internal class ActualFormDataPart(
    val name: String,
    val filename: String? = null,
    val message: ActualHttpMessage,
)
