package io.github.ackeecz.apythia.http.okhttp

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withTimeout
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.seconds

private val AWAIT_TIMEOUT = 5.seconds

private lateinit var server: MockWebServer
private lateinit var client: OkHttpClient
private lateinit var underTest: ResponseProviderDispatcher

internal class ResponseProviderDispatcherTest : FunSpec({

    beforeEach {
        underTest = ResponseProviderDispatcher()
        server = MockWebServer()
        server.dispatcher = underTest
        server.start()
        client = OkHttpClient.Builder()
            .retryOnConnectionFailure(false)
            .build()
    }

    afterEach {
        runCatching { server.close() }
    }

    test("dispatches factories in enqueue order") {
        underTest.enqueue { MockResponse(code = 201) }
        underTest.enqueue { MockResponse(code = 202) }

        sendRequest().code shouldBe 201
        sendRequest().code shouldBe 202
    }

    test("passes the recorded request to the factory") {
        underTest.enqueue { request ->
            MockResponse(code = requireNotNull(request.url.queryParameter("code")).toInt())
        }

        sendRequest(query = "?code=418").code shouldBe 418
    }

    test("close releases a request that arrived with no enqueued factory") {
        val call = async(Dispatchers.IO) { runCatching { sendRequest() } }
        server.takeRequest(AWAIT_TIMEOUT.inWholeSeconds, TimeUnit.SECONDS).shouldNotBeNull()

        server.close()

        withTimeout(AWAIT_TIMEOUT) { call.await() }
    }
})

private fun sendRequest(query: String = ""): okhttp3.Response {
    val request = Request.Builder().url("${server.url("/")}$query").build()
    return client.newCall(request).execute().also { it.close() }
}
