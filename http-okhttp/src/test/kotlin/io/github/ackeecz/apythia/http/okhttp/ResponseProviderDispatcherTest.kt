package io.github.ackeecz.apythia.http.okhttp

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

private const val AWAIT_TIMEOUT_SECONDS = 5L

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
            MockResponse(code = request.url.queryParameter("code")!!.toInt())
        }

        sendRequest(query = "?code=418").code shouldBe 418
    }

    test("close releases a request that arrived with no enqueued factory") {
        val callFinished = CountDownLatch(1)
        thread {
            runCatching { sendRequest() }
            callFinished.countDown()
        }
        server.takeRequest(AWAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS).shouldNotBeNull()

        server.close()

        callFinished.await(AWAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS) shouldBe true
    }
})

private fun sendRequest(query: String = ""): okhttp3.Response {
    val request = Request.Builder().url("${server.url("/")}$query").build()
    return client.newCall(request).execute().also { it.close() }
}
