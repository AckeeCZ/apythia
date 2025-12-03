package io.github.ackeecz.apythia.http.ktor

import io.github.ackeecz.apythia.testing.http.ArrangedResponse
import io.github.ackeecz.apythia.testing.http.BaseHttpApythiaImplTest
import io.github.ackeecz.apythia.testing.http.RemoteDataSource
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsBytes
import io.ktor.http.HttpHeaders
import io.ktor.http.headers
import io.ktor.util.appendAll
import io.ktor.util.toMap
import io.ktor.utils.io.core.buildPacket
import kotlinx.io.readByteArray
import kotlinx.io.writeString

class KtorHttpApythiaTest : BaseHttpApythiaImplTest<KtorHttpApythia>() {

    override fun createSut(): KtorHttpApythia = KtorHttpApythia()

    override fun createRemoteDataSource(sut: KtorHttpApythia): RemoteDataSource {
        return KtorRemoteDataSource(sut.mockEngine)
    }
}

private class KtorRemoteDataSource(engine: MockEngine) : RemoteDataSource {

    override val baseUrl = BASE_URL

    private val ktorClient = HttpClient(engine).config {
        setUpDefaultRequest()
        setUpLogging()
    }

    private fun HttpClientConfig<*>.setUpDefaultRequest() {
        defaultRequest {
            url(BASE_URL)
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

    override suspend fun sendPostRequest(
        url: String,
        headers: Map<String, String>,
        body: ByteArray,
    ) {
        ktorClient.post(url) {
            this.headers { appendAll(headers) }
            this.setBody(body)
        }
    }

    override suspend fun sendMultipartRequest(
        eachPartHeaders: Map<String, String>,
        partNamesToBodies: Map<String, ByteArray>,
    ) {
        val formDataParts = formData {
            partNamesToBodies.forEach { (name, body) ->
                append(
                    key = name,
                    value = body,
                    headers = headers { appendAll(eachPartHeaders) },
                )
            }
        }
        ktorClient.post("multipart") {
            setBody(MultiPartFormDataContent(formDataParts))
        }
    }

    override suspend fun sendNestedMultipartRequest(
        nestedPartNamesToBodies: Map<String, Map<String, ByteArray>>
    ) {
        val formDataParts = formData {
            nestedPartNamesToBodies.forEach { (name, nestedParts) ->
                val nestedBoundary = "nestedBoundary"
                val nestedContent = buildPacket {
                    nestedParts.forEach { (nestedName, nestedBody) ->
                        writeString("--$nestedBoundary\r\n")
                        writeString("Content-Disposition: form-data; name=\"$nestedName\"\r\n")
                        writeString("Content-Type: text/plain\r\n\r\n")
                        write(nestedBody)
                        writeString("\r\n")
                    }
                    writeString("--$nestedBoundary--\r\n")
                }
                append(
                    key = name,
                    headers = headers {
                        append(HttpHeaders.ContentType, "multipart/form-data; boundary=$nestedBoundary")
                    },
                    value = nestedContent.readByteArray(),
                )
            }
        }
        ktorClient.post("nested-multipart") {
            setBody(MultiPartFormDataContent(formDataParts))
        }
    }

    companion object {

        private const val BASE_URL = "https://www.test.com/"
    }
}
