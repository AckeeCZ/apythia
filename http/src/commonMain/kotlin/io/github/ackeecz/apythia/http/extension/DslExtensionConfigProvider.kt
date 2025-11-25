package io.github.ackeecz.apythia.http.extension

import io.github.ackeecz.apythia.http.ExperimentalHttpApi
import io.github.ackeecz.apythia.http.HttpApythia
import kotlin.reflect.KClass

/**
 * Provides access to [HttpDslExtension.Config]s on appropriate places in DSLs to be able to configure
 * your own [HttpDslExtension]s.
 */
@ExperimentalHttpApi
public interface DslExtensionConfigProvider {

    /**
     * Returns the [HttpDslExtension.Config] of the specified type if it was added during
     * [HttpApythia] creation, otherwise returns `null`.
     */
    public fun <T : HttpDslExtension.Config> getDslExtensionConfig(configType: KClass<T>): T?
}

/**
 * Returns the [HttpDslExtension.Config] of the specified type if it was added during
 * [HttpApythia] creation, otherwise returns `null`.
 */
@ExperimentalHttpApi
public inline fun <reified T : HttpDslExtension.Config> DslExtensionConfigProvider.getDslExtensionConfig(): T? {
    return getDslExtensionConfig(T::class)
}

internal class DslExtensionConfigProviderImpl(
    private val configs: List<HttpDslExtension.Config>,
) : DslExtensionConfigProvider {

    @Suppress("UNCHECKED_CAST")
    override fun <T : HttpDslExtension.Config> getDslExtensionConfig(configType: KClass<T>): T? {
        return configs.find { it::class == configType }?.let { config ->
            config as T
        }
    }
}
