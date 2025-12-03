package io.github.ackeecz.apythia.http.extension

import io.github.ackeecz.apythia.http.ExperimentalHttpApi

/**
 * DSL for adding [DslExtensionConfig]s.
 */
@ConfigsDslMarker
@ExperimentalHttpApi
public interface DslExtensionConfigs {

    /**
     * Adds a [DslExtensionConfig]. You can add only a single config of each type.
     */
    public fun dslExtensionConfig(config: DslExtensionConfig)
}

internal class DslExtensionConfigsImpl : DslExtensionConfigs {

    private val _configs = mutableListOf<DslExtensionConfig>()
    val configs: List<DslExtensionConfig> get() = _configs.toList()

    override fun dslExtensionConfig(config: DslExtensionConfig) {
        if (_configs.any { it::class == config::class }) {
            error("Same config type is already set.")
        }
        _configs.add(config)
    }
}
