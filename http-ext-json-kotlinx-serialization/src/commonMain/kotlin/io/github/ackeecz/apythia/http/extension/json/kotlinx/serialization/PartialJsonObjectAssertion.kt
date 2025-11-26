package io.github.ackeecz.apythia.http.extension.json.kotlinx.serialization

import io.github.ackeecz.apythia.http.ExperimentalHttpApi
import io.github.ackeecz.apythia.http.request.dsl.HttpRequestDslMarker
import io.kotest.assertions.withClue
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull

@HttpRequestDslMarker
@ExperimentalHttpApi
public interface PartialJsonObjectAssertion {

    /**
     * Asserts a JSON property with the given [name] and `null` value, i.e. `{"name": null}`.
     */
    public fun nullProperty(name: String)

    /**
     * Asserts a JSON property with the given [name] and [value].
     */
    public fun property(name: String, value: String)

    /**
     * Same as String [property] overload but for [Int] values.
     */
    public fun property(name: String, value: Int)

    /**
     * Same as String [property] overload but for [Long] values.
     */
    public fun property(name: String, value: Long)

    /**
     * Same as String [property] overload but for [Float] values.
     */
    public fun property(name: String, value: Float)

    /**
     * Same as String [property] overload but for [Double] values.
     */
    public fun property(name: String, value: Double)

    /**
     * Same as String [property] overload but for [Boolean] values.
     */
    public fun property(name: String, value: Boolean)

    public fun missingProperties(vararg name: String)

    /**
     * Asserts a JSON property with the given [name] and [value]. Property is expected to have either
     * a present non-null [value] or be missing in JSON object if [value] is `null`.
     */
    public fun propertyWithPresentValueOrMissing(name: String, value: String?)

    /**
     * Same as String [propertyWithPresentValueOrMissing] overload but for [Int] values.
     */
    public fun propertyWithPresentValueOrMissing(name: String, value: Int?)

    /**
     * Same as String [propertyWithPresentValueOrMissing] overload but for [Long] values.
     */
    public fun propertyWithPresentValueOrMissing(name: String, value: Long?)

    /**
     * Same as String [propertyWithPresentValueOrMissing] overload but for [Float] values.
     */
    public fun propertyWithPresentValueOrMissing(name: String, value: Float?)

    /**
     * Same as String [propertyWithPresentValueOrMissing] overload but for [Double] values.
     */
    public fun propertyWithPresentValueOrMissing(name: String, value: Double?)

    /**
     * Same as String [propertyWithPresentValueOrMissing] overload but for [Boolean] values.
     */
    public fun propertyWithPresentValueOrMissing(name: String, value: Boolean?)
}

internal class PartialJsonObjectAssertionImpl(
    private val actualJsonObject: JsonObject,
) : PartialJsonObjectAssertion {

    override fun nullProperty(name: String) {
        val actualValue = assertValuePresent(name)
        withClue("Property '$name' is not a JSON null") {
            actualValue.jsonNull
        }
    }

    private fun assertValuePresent(name: String): JsonElement {
        return withClue("Property '$name' not found in JSON object") {
            actualJsonObject[name].shouldNotBeNull()
        }
    }

    override fun property(name: String, value: String) {
        assertProperty(
            name = name,
            expectedTypeName = "string",
            assertValueType = { jsonPrimitive ->
                jsonPrimitive.isString.shouldBeTrue()
                jsonPrimitive.content
            },
            assertValue = { it shouldBe value },
        )
    }

    private fun <T> assertProperty(
        name: String,
        expectedTypeName: String,
        assertValueType: (JsonPrimitive) -> T,
        assertValue: (T) -> Unit,
    ) {
        val actualValue = assertValuePresent(name)
        val jsonPrimitive = assertPrimitive(name, actualValue)
        val actualTypedValue = withClue("Property '$name' is not a $expectedTypeName") {
            assertValueType(jsonPrimitive)
        }
        assertValue(actualTypedValue)
    }

    private fun assertPrimitive(name: String, value: JsonElement): JsonPrimitive {
        return withClue("Property '$name' is not a JSON primitive") {
            value.jsonPrimitive
        }
    }

    override fun property(name: String, value: Int) {
        assertProperty(
            name = name,
            expectedTypeName = "int",
            assertValueType = { jsonPrimitive ->
                assertTypeButNotString(jsonPrimitive) { it.intOrNull.shouldNotBeNull() }
            },
            assertValue = { it shouldBe value },
        )
    }

    private fun <T> assertTypeButNotString(
        primitive: JsonPrimitive,
        assertType: (JsonPrimitive) -> T,
    ): T {
        primitive.isString.shouldBeFalse()
        return assertType(primitive)
    }

    override fun property(name: String, value: Long) {
        assertProperty(
            name = name,
            expectedTypeName = "long",
            assertValueType = { jsonPrimitive ->
                assertTypeButNotString(jsonPrimitive) { it.longOrNull.shouldNotBeNull() }
            },
            assertValue = { it shouldBe value },
        )
    }

    override fun property(name: String, value: Float) {
        assertProperty(
            name = name,
            expectedTypeName = "float",
            assertValueType = { jsonPrimitive ->
                assertTypeButNotString(jsonPrimitive) { it.floatOrNull.shouldNotBeNull() }
            },
            assertValue = { it shouldBe value },
        )
    }

    override fun property(name: String, value: Double) {
        assertProperty(
            name = name,
            expectedTypeName = "double",
            assertValueType = { jsonPrimitive ->
                assertTypeButNotString(jsonPrimitive) { it.doubleOrNull.shouldNotBeNull() }
            },
            assertValue = { it shouldBe value },
        )
    }

    override fun property(name: String, value: Boolean) {
        assertProperty(
            name = name,
            expectedTypeName = "boolean",
            assertValueType = { jsonPrimitive ->
                assertTypeButNotString(jsonPrimitive) { it.booleanOrNull.shouldNotBeNull() }
            },
            assertValue = { it shouldBe value },
        )
    }

    override fun missingProperties(vararg name: String) {
        actualJsonObject.keys.intersect(name.toSet()).shouldBeEmpty()
    }

    override fun propertyWithPresentValueOrMissing(name: String, value: String?) {
        if (value == null) {
            missingProperties(name)
        } else {
            property(name, value)
        }
    }

    override fun propertyWithPresentValueOrMissing(name: String, value: Int?) {
        if (value == null) {
            missingProperties(name)
        } else {
            property(name, value)
        }
    }

    override fun propertyWithPresentValueOrMissing(name: String, value: Long?) {
        if (value == null) {
            missingProperties(name)
        } else {
            property(name, value)
        }
    }

    override fun propertyWithPresentValueOrMissing(name: String, value: Float?) {
        if (value == null) {
            missingProperties(name)
        } else {
            property(name, value)
        }
    }

    override fun propertyWithPresentValueOrMissing(name: String, value: Double?) {
        if (value == null) {
            missingProperties(name)
        } else {
            property(name, value)
        }
    }

    override fun propertyWithPresentValueOrMissing(name: String, value: Boolean?) {
        if (value == null) {
            missingProperties(name)
        } else {
            property(name, value)
        }
    }
}
