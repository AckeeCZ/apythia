package io.github.ackeecz.apythia.http.extension

import io.github.ackeecz.apythia.http.ExperimentalHttpApi
import io.github.ackeecz.apythia.http.HttpApythia
import kotlin.reflect.KClass

/**
 * Provides access to [DslExtensionConfig]s on appropriate places in DSLs to be able to configure
 * your own DSL extensions.
 */
@ExperimentalHttpApi
public interface DslExtensionConfigProvider {

    /**
     * Returns the [DslExtensionConfig] of the specified type if it was added during
     * [HttpApythia] creation, otherwise returns `null`.
     */
    public fun <T : DslExtensionConfig> getDslExtensionConfig(configType: KClass<T>): T?
}

/**
 * Returns the [DslExtensionConfig] of the specified type if it was added during
 * [HttpApythia] creation, otherwise returns `null`.
 */
@ExperimentalHttpApi
public inline fun <reified T : DslExtensionConfig> DslExtensionConfigProvider.getDslExtensionConfig(): T? {
    return getDslExtensionConfig(T::class)
}

internal class DslExtensionConfigProviderImpl(
    private val configs: List<DslExtensionConfig>,
) : DslExtensionConfigProvider {

    @Suppress("UNCHECKED_CAST")
    override fun <T : DslExtensionConfig> getDslExtensionConfig(configType: KClass<T>): T? {
        return configs.find { it::class == configType }?.let { config ->
            config as T
        }
    }
}
