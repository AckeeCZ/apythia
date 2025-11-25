package io.github.ackeecz.apythia.http.request.dsl.url

import io.github.ackeecz.apythia.http.request.dsl.HttpRequestAssertionImpl
import io.github.ackeecz.apythia.http.request.dsl.createHttpRequestAssertionImpl
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.spec.style.scopes.FunSpecContainerScope
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

private lateinit var underTest: HttpRequestAssertionImpl

class UrlAssertionTest : FunSpec({

    beforeEach {
        underTest = createHttpRequestAssertionImpl()
    }

    urlTests()
    pathTests()
    pathSuffixTests()
    queryTests()
})

private fun FunSpec.urlTests() {
    context("url") {
        notAssertedTestSuite { underTest.url }

        test("set") {
            val expected = "https://example.com/path"

            underTest.url { url(expected) }

            underTest.url shouldBe expected
        }

        test("can be called only once") {
            underTest.url {
                url("https://example.com/path")

                shouldThrow<IllegalStateException> {
                    url("https://example.com/another-path")
                }
            }
        }
    }
}

private fun FunSpec.pathTests() {
    context("path") {
        notAssertedTestSuite { underTest.path }

        test("set") {
            val expected = "/path"

            underTest.url { path(expected) }

            underTest.path shouldBe expected
        }

        test("can be called only once") {
            underTest.url {
                path("/path")

                shouldThrow<IllegalStateException> {
                    path("/another-path")
                }
            }
        }
    }
}

private fun FunSpec.pathSuffixTests() {
    context("pathSuffix") {
        notAssertedTestSuite { underTest.pathSuffix }

        test("set") {
            val expected = "/path"

            underTest.url { pathSuffix(expected) }

            underTest.pathSuffix shouldBe expected
        }

        test("can be called only once") {
            underTest.url {
                pathSuffix("/path")

                shouldThrow<IllegalStateException> {
                    pathSuffix("/another-path")
                }
            }
        }
    }
}

private fun FunSpec.queryTests() {
    context("query") {
        test("can be called only once") {
            underTest.url {
                query {}

                shouldThrow<IllegalStateException> {
                    query {}
                }
            }
        }
    }
}

private suspend fun FunSpecContainerScope.notAssertedTestSuite(
    getNotAssertedProperty: () -> Any?,
) {
    test("not asserted by default") {
        getNotAssertedProperty().shouldBeNull()
    }

    test("not asserted when just empty query assertion block is called") {
        underTest.url {
            query {}
        }

        getNotAssertedProperty().shouldBeNull()
    }
}

internal val HttpRequestAssertionImpl.expectedUrl
    get() = expectedRequest.url

private val HttpRequestAssertionImpl.url
    get() = expectedUrl.url

private val HttpRequestAssertionImpl.path
    get() = expectedUrl.path

private val HttpRequestAssertionImpl.pathSuffix
    get() = expectedUrl.pathSuffix
