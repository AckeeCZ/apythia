package io.github.ackeecz.apythia.http.extension

import com.eygraber.uri.Url
import io.github.ackeecz.apythia.http.request.ActualHttpMessage
import io.github.ackeecz.apythia.http.request.RequestTargetWithMessage

internal class HttpDslExtensionMock : HttpDslExtension {

    var dataToAssert: RequestTargetWithMessage? = null
        private set

    var failAssertion: Boolean = false

    override fun assert(
        method: String,
        url: String,
        message: ActualHttpMessage,
    ) {
        dataToAssert = RequestTargetWithMessage(method, Url.parse(url), message)
        if (failAssertion) {
            throw AssertionError()
        }
    }

    class Config : HttpDslExtension.Config {

        var data: Any? = null
    }
}
