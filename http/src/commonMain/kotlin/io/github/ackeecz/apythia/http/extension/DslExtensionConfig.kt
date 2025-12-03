package io.github.ackeecz.apythia.http.extension

import io.github.ackeecz.apythia.http.ExperimentalHttpApi
import io.github.ackeecz.apythia.http.HttpApythia
import io.github.ackeecz.apythia.http.request.dsl.body.BodyAssertion
import io.github.ackeecz.apythia.http.response.dsl.HttpResponseArrangement

/**
 * Configuration for custom DSL extensions. When you implement custom DSL extensions to extend
 * arrangement/assertion capabilities, you might want
 * to provide the user with a possibility to configure it. For example, when implementing the
 * extension for asserting JSON bodies, you might want to let the user to specify JSON encoder
 * configuration. You could declare a parameter on your DSL extension method (e.g. extension on [BodyAssertion]),
 * but the client of your DSL extension would have to pass the configuration each time they use
 * it. Instead, you can implement a [DslExtensionConfig] object that the client can use to configure your
 * extension globally. [DslExtensionConfig]s can be passed to the [HttpApythia] when creating it. Apythia
 * then provides you with this config on appropriate places like [BodyAssertion] or
 * [HttpResponseArrangement], so you can access it in your own DSL methods.
 *
 * If you want to implement your own [DslExtensionConfig], you can check the existing extension for JSON
 * backed by Kotlinx Serialization provided in a separate library artifact.
 */
@ExperimentalHttpApi
public interface DslExtensionConfig
