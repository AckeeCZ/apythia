package io.github.ackeecz.apythia.sample

import io.github.ackeecz.apythia.http.HttpApythia
import io.github.ackeecz.apythia.http.extension.json.kotlinx.serialization.jsonObjectBody
import io.github.ackeecz.apythia.http.ktor.KtorHttpApythia
import io.github.ackeecz.apythia.http.okhttp.OkHttpHttpApythia
import io.kotest.core.spec.style.FunSpec
import kotlinx.serialization.json.put

/**
 * This serves only as smoke tests of [HttpApythia] implementations to ensure that published
 * artifacts work as expected, i.e. dependencies are correctly resolved and basic implementation
 * paths do not fail at runtime.
 */
abstract class HttpApythiaSmokeTest : FunSpec() {

    private val underTest: HttpApythia = createSut()

    init {
        beforeEach {
            underTest.beforeEachTest()
        }
        afterEach {
            underTest.afterEachTest()
        }

        test("mocking") {
            underTest.mockNextResponse {
                statusCode(200)
                headers {
                    headers("X-Custom-Header", listOf("value"))
                }
                jsonObjectBody {
                    put("key", 1.0)
                }
            }
        }
    }

    protected abstract fun createSut(): HttpApythia
}

class KtorHttpApythiaSmokeTest : HttpApythiaSmokeTest() {

    override fun createSut() = KtorHttpApythia()
}

class OkHttpHttpApythiaSmokeTest : HttpApythiaSmokeTest() {

    override fun createSut() = OkHttpHttpApythia()
}
