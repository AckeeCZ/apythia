package io.github.ackeecz.apythia.http.extension

import io.github.ackeecz.apythia.http.ExperimentalHttpApi
import io.github.ackeecz.apythia.http.HttpApythia
import io.github.ackeecz.apythia.http.request.ActualHttpMessage
import io.github.ackeecz.apythia.http.request.dsl.body.BodyAssertion
import io.github.ackeecz.apythia.http.response.dsl.HttpResponseArrangement

/**
 * [HttpDslExtension] enables to extend HTTP DSLs capabilities. For example, you can extend
 * [BodyAssertion] to add custom assertions for HTTP body that are not provided by this library, e.g.
 * XML body assertions using any library you want.
 *
 * If you want to implement your own extension, you can check the existing extension for JSON
 * backed by Kotlinx Serialization provided in a separate library artifact.
 */
@ExperimentalHttpApi
public interface HttpDslExtension {

    /**
     * Called by [HttpApythia] to assert HTTP request. The exact semantics of the [assert] call is
     * determined by the particular usage of the extension. For example if you use the extension
     * to assert request body using [BodyAssertion.dslExtension], [assert] is called for asserting
     * the request body, so even though you can assert here anything you want, you should semantically
     * assert the request body to fulfill the expectations of the client of your extension.
     *
     * @param method actual request HTTP method.
     * @param url actual request URL.
     * @param message actual message. This can represent the message of either a HTTP request or a
     * multipart part, depending on the context of the extension usage.
     */
    public fun assert(
        method: String,
        url: String,
        message: ActualHttpMessage,
    )

    /**
     * Configuration for [HttpDslExtension]. When you implement [HttpDslExtension], you might want
     * to provide the user with a possibility to configure it. For example, when implementing the
     * extension for asserting JSON bodies, you might want to let the user to specify JSON encoder
     * configuration. You could declare a parameter on your extension method on the DSL (e.g. [BodyAssertion]),
     * but the client of your DSL extension would have to pass the configuration each time they use
     * it. Instead, you can implement a [Config] object that the client can use to configure your
     * extension globally. [Config]s can be passed to the [HttpApythia] when creating it. Apythia
     * then provides you with this config on the appropriate places like [BodyAssertion] or
     * [HttpResponseArrangement], so you can access it in your own DSL methods and provide it to your
     * [HttpDslExtension].
     *
     * If you want to implement your own [Config], you can check the existing extension for JSON
     * backed by Kotlinx Serialization provided in a separate library artifact.
     */
    @ExperimentalHttpApi
    public interface Config
}
