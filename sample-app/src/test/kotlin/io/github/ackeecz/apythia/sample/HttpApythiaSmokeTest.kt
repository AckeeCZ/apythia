package io.github.ackeecz.apythia.sample

import io.github.ackeecz.apythia.http.ExperimentalHttpApi
import io.github.ackeecz.apythia.http.HttpApythia
import io.github.ackeecz.apythia.http.ktor.KtorHttpApythia
import io.github.ackeecz.apythia.http.okhttp.OkHttpHttpApythia
import io.kotest.core.spec.style.FunSpec

/**
 * This serves only as smoke tests of [HttpApythia] implementations to ensure that published
 * artifacts work as expected, i.e. dependencies are correctly resolved and basic implementation
 * paths do not fail at runtime.
 */
@OptIn(ExperimentalHttpApi::class)
abstract class HttpApythiaSmokeTest : FunSpec() {

    private lateinit var underTest: HttpApythia

    init {
        beforeEach {
            underTest = createSut().also { it.beforeEachTest() }
        }
        afterEach {
            underTest.afterEachTest()
        }

        test("arrangement") {
            underTest.arrangeNextResponse {
                statusCode(200)
                headers {
                    headers("X-Custom-Header", listOf("value"))
                }
                bytesBody(byteArrayOf(1, 2, 3), contentType = null)
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
