package io.github.ackeecz.apythia.http.extension

import io.github.ackeecz.apythia.http.ExperimentalHttpApi

/**
 * DSL for adding [HttpDslExtension.Config]s.
 */
@ConfigsDslMarker
@ExperimentalHttpApi
public interface DslExtensionConfigs {

    /**
     * Adds a [HttpDslExtension.Config]. You can add only a single config of each type.
     */
    public fun dslExtensionConfig(config: HttpDslExtension.Config)
}

internal class DslExtensionConfigsImpl : DslExtensionConfigs {

    private val _configs = mutableListOf<HttpDslExtension.Config>()
    val configs: List<HttpDslExtension.Config> get() = _configs.toList()

    override fun dslExtensionConfig(config: HttpDslExtension.Config) {
        if (configs.any { it::class == config::class }) {
            error("Same config type is already set.")
        }
        _configs.add(config)
    }
}
