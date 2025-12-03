package io.github.ackeecz.apythia.http.testing

public interface RemoteDataSource {

    public suspend fun getArrangedResponse(): ArrangedResponse
}

public class ArrangedResponse(
    public val statusCode: Int,
    public val headers: Map<String, List<String>>,
    public val body: ByteArray,
)
