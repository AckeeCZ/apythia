package io.github.ackeecz.apythia.http.ktor

import io.github.ackeecz.apythia.http.request.ActualRequest
import io.github.ackeecz.apythia.http.response.dsl.HttpResponseMockBuilder
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsBytes
import io.ktor.utils.io.ByteReadChannel

private const val BASE_URL = "https://www.test.com/"

private lateinit var underTest: KtorHttpApythia
private lateinit var client: HttpClient

/**
 * Converting a request to [ActualRequest] reads its body, which is not repeatable for streamed
 * bodies. These tests cover that a dynamically mocked response and a following request assertion
 * both see the request body, even though only one of them can read it from the request.
 */
internal class KtorDynamicResponseTest : FunSpec({

    beforeEach {
        underTest = KtorHttpApythia()
        underTest.beforeEachTest()
        client = HttpClient(underTest.mockEngine)
    }

    afterEach {
        underTest.afterEachTest()
    }

    test("dynamic response and request assertion both see a streamed request body") {
        val expectedBody = byteArrayOf(1, 2, 3)
        underTest.mockNextDynamicResponse(echoRequestBody)

        val actual = postStreamedBody(expectedBody)

        actual.bodyAsBytes() shouldBe expectedBody
        assertNextRequestBody(expectedBody)
    }

    test("dynamic response and request assertion both see a non-streamed request body") {
        val expectedBody = byteArrayOf(1, 2, 3)
        underTest.mockNextDynamicResponse(echoRequestBody)

        val actual = client.post(BASE_URL) { setBody(expectedBody) }

        actual.bodyAsBytes() shouldBe expectedBody
        assertNextRequestBody(expectedBody)
    }

    test("each dynamically mocked request is asserted with its own streamed body") {
        val firstExpectedBody = byteArrayOf(1, 2, 3)
        val secondExpectedBody = byteArrayOf(4, 5, 6)
        underTest.mockNextDynamicResponses(count = 2, mock = echoRequestBody)

        postStreamedBody(firstExpectedBody)
        postStreamedBody(secondExpectedBody)

        assertNextRequestBody(firstExpectedBody)
        assertNextRequestBody(secondExpectedBody)
    }
})

private val echoRequestBody: HttpResponseMockBuilder.(ActualRequest) -> Unit = { request ->
    bytesBody(request.body, contentType = null)
}

private suspend fun postStreamedBody(body: ByteArray): HttpResponse {
    return client.post(BASE_URL) { setBody(ByteReadChannel(body)) }
}

private suspend fun assertNextRequestBody(expected: ByteArray) {
    underTest.assertNextRequest {
        body { bytes(expected) }
    }
}
