package io.github.ackeecz.apythia.http.okhttp

import mockwebserver3.Dispatcher
import mockwebserver3.MockResponse
import mockwebserver3.RecordedRequest
import java.net.HttpURLConnection
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

/**
 * [Dispatcher] serving responses built by one-shot factories, which receive the arriving
 * [RecordedRequest]. This makes it possible to derive a mocked response from the request that it
 * responds to, which the default `QueueDispatcher` does not support, because it serves pre-built
 * responses without ever consulting the request.
 */
internal class ResponseProviderDispatcher : Dispatcher() {

    private val responseProviders: BlockingQueue<(RecordedRequest) -> MockResponse> = LinkedBlockingQueue()

    /**
     * Appends a one-shot response factory. Factories are consumed in the order they are enqueued.
     */
    fun enqueue(provideResponse: (RecordedRequest) -> MockResponse) {
        responseProviders.add(provideResponse)
    }

    override fun dispatch(request: RecordedRequest): MockResponse {
        val provideResponse = responseProviders.take()
        // If take() returned the dead letter, then the server is shutting down. Enqueue the dead
        // letter back, so any other threads waiting on take() are released as well.
        if (provideResponse === DEAD_LETTER) {
            responseProviders.add(DEAD_LETTER)
        }
        return provideResponse(request)
    }

    override fun close() {
        responseProviders.add(DEAD_LETTER)
    }

    companion object {

        /**
         * Enqueued on shutdown to release threads waiting in [dispatch]. This response is not
         * transmitted, because the connection is closed before it is returned.
         */
        private val DEAD_LETTER: (RecordedRequest) -> MockResponse = {
            MockResponse(code = HttpURLConnection.HTTP_UNAVAILABLE)
        }
    }
}
