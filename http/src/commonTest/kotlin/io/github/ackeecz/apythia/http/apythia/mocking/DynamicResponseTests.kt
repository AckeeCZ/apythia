package io.github.ackeecz.apythia.http.apythia.mocking

import io.github.ackeecz.apythia.http.HttpApythia
import io.github.ackeecz.apythia.http.apythia.HttpApythiaTest
import io.github.ackeecz.apythia.http.request.ActualHttpMessage
import io.github.ackeecz.apythia.http.request.ActualRequest
import io.github.ackeecz.apythia.http.request.body.ActualPart
import io.github.ackeecz.apythia.http.response.HttpResponse
import io.github.ackeecz.apythia.http.response.dsl.HttpResponseMockBuilder
import io.github.ackeecz.apythia.testing.http.HttpApythiaMock
import io.github.ackeecz.apythia.testing.http.request.createActualRequest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.scopes.FunSpecContainerScope
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

private val idEchoingMock: HttpResponseMockBuilder.(ActualRequest) -> Unit = { request ->
    plainTextBody(checkNotNull(request.queryParameter("id")))
}

private fun HttpApythiaMock.mockNextIdEchoingResponse() {
    mockNextDynamicResponse(idEchoingMock)
}

private fun HttpApythiaMock.mockNextIdEchoingResponses(count: Int) {
    mockNextDynamicResponses(count, idEchoingMock)
}

private fun HttpApythiaMock.assertFailsToMockResponses(count: Int) {
    shouldThrow<IllegalArgumentException> {
        mockNextIdEchoingResponses(count)
    }

    mockedResponseProviders.shouldBeEmpty()
}

internal suspend fun FunSpecContainerScope.dynamicResponseTests(
    fixture: HttpApythiaTest.Fixture,
) = with(fixture) {
    context("dynamic response") {

        test("response is built from the request passed to the provider") {
            underTest.mockNextIdEchoingResponse()

            val actual = resolveLastMockedResponse(createActualRequest(url = "http://example.com?id=abc"))

            actual.body.decodeToString() shouldBe "abc"
        }

        test("each invocation of the provider gets a fresh builder") {
            underTest.mockNextIdEchoingResponse()
            val provider = underTest.mockedResponseProviders.last()

            val first = provider(createActualRequest(url = "http://example.com?id=1"))
            val second = provider(createActualRequest(url = "http://example.com?id=2"))

            first.body.decodeToString() shouldBe "1"
            second.body.decodeToString() shouldBe "2"
        }

        test("response defaults apply when the block sets nothing") {
            underTest.mockNextDynamicResponse {}

            val actual = resolveLastMockedResponse()

            actual.statusCode shouldBe 200
            actual.headers shouldBe emptyMap()
            actual.body shouldBe byteArrayOf()
        }

        test("static and dynamic mocks are recorded in mocking order") {
            underTest.mockNextResponse { statusCode(201) }
            underTest.mockNextDynamicResponse { statusCode(202) }
            underTest.mockNextResponse { statusCode(203) }

            val actual = underTest.mockedResponseProviders.map {
                it(underTest.actualRequest).statusCode
            }

            actual shouldBe listOf(201, 202, 203)
        }

        test("actual response is the last recorded provider resolved against the actual request") {
            underTest.mockNextIdEchoingResponse()
            underTest.actualRequestUrl = "http://example.com?id=xyz"

            requireActualResponse().body.decodeToString() shouldBe "xyz"
        }
    }

    context("dynamic responses") {

        test("records the provider count times") {
            underTest.mockNextIdEchoingResponses(count = 3)

            val actual = underTest.mockedResponseProviders.mapIndexed { index, provideResponse ->
                provideResponse(createActualRequest(url = "http://example.com?id=$index"))
                    .body
                    .decodeToString()
            }

            actual shouldBe listOf("0", "1", "2")
        }

        test("fails for zero count") {
            underTest.assertFailsToMockResponses(count = 0)
        }

        test("fails for negative count") {
            underTest.assertFailsToMockResponses(count = -1)
        }
    }

    context("default dynamic response implementation") {

        test("mockNextDynamicResponse fails at mock time with IllegalStateException naming the implementation") {
            val underTest = StaticOnlyHttpApythiaStub()

            val actual = shouldThrow<IllegalStateException> {
                underTest.mockNextDynamicResponse {}
            }

            actual.assertUnsupportedDynamicResponses()
        }

        test("mockNextDynamicResponses fails at mock time with IllegalStateException naming the implementation") {
            val underTest = StaticOnlyHttpApythiaStub()

            val actual = shouldThrow<IllegalStateException> {
                underTest.mockNextDynamicResponses(count = 1) {}
            }

            actual.assertUnsupportedDynamicResponses()
        }
    }
}

private fun IllegalStateException.assertUnsupportedDynamicResponses() {
    val actualMessage = message.shouldNotBeNull()

    actualMessage shouldContain checkNotNull(StaticOnlyHttpApythiaStub::class.simpleName)
    actualMessage shouldContain "dynamic responses"
}

private class StaticOnlyHttpApythiaStub : HttpApythia(dslExtensionConfigs = {}) {

    override fun beforeEachTest(): Unit = Unit

    override fun afterEachTest(): Unit = Unit

    override fun mockNextResponse(response: HttpResponse): Unit = Unit

    override suspend fun getNextActualRequest(): ActualRequest = createActualRequest()

    override suspend fun forEachMultipartFormDataPart(
        message: ActualHttpMessage,
        onPart: suspend (ActualPart) -> Unit,
    ): Unit = Unit
}
