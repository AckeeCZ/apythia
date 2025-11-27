package io.github.ackeecz.apythia.testing.http

public interface RemoteDataSource {

    public val baseUrl: String

    public suspend fun getMockedResponse(): MockedResponse

    public suspend fun sendPostRequest(
        url: String = baseUrl,
        headers: Map<String, String> = emptyMap(),
        body: ByteArray = byteArrayOf(),
    )

    public suspend fun sendMultipartRequest(
        eachPartHeaders: Map<String, String>,
        partNamesToBodies: Map<String, ByteArray>,
    )

    public suspend fun sendNestedMultipartRequest(
        nestedPartNamesToBodies: Map<String, Map<String, ByteArray>>,
    )
}

public class MockedResponse(
    public val statusCode: Int,
    public val headers: Map<String, List<String>>,
    public val body: ByteArray,
)
