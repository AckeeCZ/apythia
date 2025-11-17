package io.github.ackeecz.apythia.http.okhttp

import io.github.ackeecz.apythia.http.testing.ArrangedResponse
import io.github.ackeecz.apythia.http.testing.HttpApythiaTest
import io.github.ackeecz.apythia.http.testing.RemoteDataSource
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET

class OkHttpHttpApythiaTest : HttpApythiaTest<OkHttpHttpApythia>() {

    override fun createSut(): OkHttpHttpApythia = OkHttpHttpApythia()

    override fun createRemoteDataSource(sut: OkHttpHttpApythia): RemoteDataSource {
        return RetrofitRemoteDataSource(sut)
    }
}

private class RetrofitRemoteDataSource(apythia: OkHttpHttpApythia) : RemoteDataSource {

    private val apiDescription = Retrofit.Builder()
        .baseUrl(apythia.getMockWebServerUrl())
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

    private interface ApiDescription {

        @GET("arranged-response")
        suspend fun getArrangedResponse(): Response<ResponseBody>
    }
}
