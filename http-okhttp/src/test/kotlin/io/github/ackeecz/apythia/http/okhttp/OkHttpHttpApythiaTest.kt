package io.github.ackeecz.apythia.http.okhttp

import io.github.ackeecz.apythia.http.testing.ArrangedResponse
import io.github.ackeecz.apythia.http.testing.BaseHttpApythiaImplTest
import io.github.ackeecz.apythia.http.testing.RemoteDataSource
import okhttp3.Headers
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.POST
import retrofit2.http.Url
import kotlin.collections.component1
import kotlin.collections.component2

class OkHttpHttpApythiaTest : BaseHttpApythiaImplTest<OkHttpHttpApythia>() {

    override fun createSut(): OkHttpHttpApythia = OkHttpHttpApythia()

    override fun createRemoteDataSource(sut: OkHttpHttpApythia): RemoteDataSource {
        return RetrofitRemoteDataSource(sut)
    }
}

private class RetrofitRemoteDataSource(apythia: OkHttpHttpApythia) : RemoteDataSource {

    override val baseUrl = apythia.getMockWebServerUrl()

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        .build()

    private val apiDescription = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .build()
        .create(ApiDescription::class.java)

    override suspend fun getArrangedResponse(): ArrangedResponse {
        val response = apiDescription.getArrangedResponse()
        return ArrangedResponse(
            statusCode = response.code(),
            // Content-Length header is automatically added by OkHttp's mock web server, so we need to
            // remove it to preserve arranged headers.
            headers = response.headers().toMultimap().removeContentLength(),
            body = response.body()?.bytes() ?: byteArrayOf(),
        )
    }

    private fun Map<String, List<String>>.removeContentLength(): Map<String, List<String>> {
        return filterNot { (key, _) -> key.lowercase() == "content-length" }
    }

    override suspend fun sendPostRequest(
        url: String,
        headers: Map<String, String>,
        body: ByteArray,
    ) {
        apiDescription.sendPostRequest(
            url = url,
            headers = headers,
            body = body.toRequestBody("image/jpeg".toMediaType()),
        )
    }

    override suspend fun sendMultipartRequest(
        eachPartHeaders: Map<String, String>,
        partNamesToBodies: Map<String, ByteArray>,
    ) {
        val multipartBuilder = MultipartBody.Builder().setType(MultipartBody.FORM)
        val normalizedHeaders = eachPartHeaders.mapKeys { (key, _) -> key.lowercase() }
        val partContentType = normalizedHeaders["content-type"] ?: "image/jpeg"
        partNamesToBodies.forEach { (name, body) ->
            val headers = Headers.Builder()
            normalizedHeaders.forEach { (name, value) -> headers.add(name, value) }
            // Remove "content-type" header from each part if present, OkHttp wants us to set
            // content-type using toRequestBody.
            headers.removeAll("content-type")
            headers.add("Content-Disposition", "form-data; name=\"$name\"")
            multipartBuilder.addPart(
                headers = headers.build(),
                body = body.toRequestBody(partContentType.toMediaType()),
            )
        }
        apiDescription.sendMultipartRequest(multipartBuilder.build())
    }

    override suspend fun sendNestedMultipartRequest(nestedPartNamesToBodies: Map<String, Map<String, ByteArray>>) {
        val multipartBuilder = MultipartBody.Builder().setType(MultipartBody.FORM)
        nestedPartNamesToBodies.forEach { (name, nestedParts) ->
            val nestedMultipartBuilder = MultipartBody.Builder().setType(MultipartBody.FORM)
            nestedParts.forEach { (nestedName, nestedBody) ->
                nestedMultipartBuilder.addFormDataPart(
                    name = nestedName,
                    filename = null,
                    body = nestedBody.toRequestBody(),
                )
            }
            multipartBuilder.addFormDataPart(
                name = name,
                filename = null,
                body = nestedMultipartBuilder.build(),
            )
        }
        apiDescription.sendMultipartRequest(multipartBuilder.build())
    }

    private interface ApiDescription {

        @GET("arranged-response")
        suspend fun getArrangedResponse(): Response<ResponseBody>

        @POST
        suspend fun sendPostRequest(
            @Url url: String,
            @HeaderMap headers: Map<String, String>,
            @Body body: RequestBody,
        )

        @POST("api/v1/multipart")
        suspend fun sendMultipartRequest(@Body multipartBody: RequestBody)
    }
}
