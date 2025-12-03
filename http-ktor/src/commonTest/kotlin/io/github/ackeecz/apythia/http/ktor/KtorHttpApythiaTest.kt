package io.github.ackeecz.apythia.http.ktor

import io.github.ackeecz.apythia.http.testing.ArrangedResponse
import io.github.ackeecz.apythia.http.testing.HttpApythiaTest
import io.github.ackeecz.apythia.http.testing.RemoteDataSource
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsBytes
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.toMap
import kotlinx.serialization.json.Json

class KtorHttpApythiaTest : HttpApythiaTest<KtorHttpApythia>() {

    override fun createSut(): KtorHttpApythia = KtorHttpApythia()

    override fun createRemoteDataSource(sut: KtorHttpApythia): RemoteDataSource {
        return KtorRemoteDataSource(sut.mockEngine)
    }
}

private class KtorRemoteDataSource(engine: MockEngine) : RemoteDataSource {

    private val ktorClient = HttpClient(engine).config {
        setUpDefaultRequest()
        setUpContentNegotiation()
        setUpLogging()
    }

    private fun HttpClientConfig<*>.setUpDefaultRequest() {
        defaultRequest {
            contentType(ContentType.Application.Json)
            url(BASE_URL)
        }
    }

    private fun HttpClientConfig<*>.setUpContentNegotiation() {
        install(ContentNegotiation) {
            json(
                Json {
                    isLenient = true
                    ignoreUnknownKeys = true
                    explicitNulls = false
                    encodeDefaults = true
                }
            )
        }
    }

    private fun HttpClientConfig<*>.setUpLogging() {
        install(Logging) {
            level = LogLevel.ALL
            logger = object : Logger {

                override fun log(message: String) {
                    println(message)
                }
            }
        }
    }

    override suspend fun getArrangedResponse(): ArrangedResponse {
        val response = ktorClient.get { url("arranged-response") }
        return ArrangedResponse(
            statusCode = response.status.value,
            headers = response.headers.toMap(),
            body = response.bodyAsBytes(),
        )
    }

    companion object {

        private const val BASE_URL = "http://www.test.com/"
    }
}
