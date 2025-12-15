package io.github.ackeecz.apythia.http.apythia.assertion

import io.github.ackeecz.apythia.http.apythia.HttpApythiaTest
import io.github.ackeecz.apythia.testing.http.shouldFail
import io.github.ackeecz.apythia.testing.http.shouldNotFail
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.scopes.FunSpecContainerScope
import io.kotest.matchers.shouldBe

internal suspend fun FunSpecContainerScope.urlTests(
    fixture: HttpApythiaTest.Fixture
) = with(fixture) {
    context("url") {
        test("block can be called just once") {
            underTest.assertNextRequest {
                url {}

                shouldThrow<IllegalStateException> {
                    url {}
                }
            }
        }

        test("get actual url") {
            val expected = "http://example.com/path?key=value"
            underTest.actualRequestUrl = expected

            underTest.assertNextRequest {
                url {
                    actualUrl shouldBe expected
                }
            }
        }

        @Suppress("MaxLineLength")
        test("actual url is decoded correctly") {
            underTest.actualRequestUrl = "https://ex+ample.com/path/with+plus/and%20space?percent%20=hello%20world&plus+=hello+world&encoded%2B=1%2B2#frag+ment%20here"

            underTest.assertNextRequest {
                url {
                    actualUrl shouldBe "https://ex+ample.com/path/with+plus/and space?percent =hello world&plus =hello world&encoded+=1+2#frag+ment here"
                }
            }
        }

        fullUrlTests(fixture)
        pathTests(fixture)
        pathSuffixTests(fixture)
        queryTests(fixture)
    }
}

private suspend fun FunSpecContainerScope.fullUrlTests(
    fixture: HttpApythiaTest.Fixture
) = with(fixture) {
    context("full url") {
        test("failure") {
            underTest.actualRequestUrl = "http://example.com"

            shouldFail {
                underTest.assertNextRequest {
                    url { url("http://another-example.com") }
                }
            }
        }

        test("success") {
            val expected = "http://example.com/path?param=value"
            underTest.actualRequestUrl = expected

            shouldNotFail {
                underTest.assertNextRequest {
                    url { url(expected) }
                }
            }
        }

        @Suppress("MaxLineLength")
        test("encoded url success") {
            underTest.actualRequestUrl = "https://ex+ample.com/path/with+plus/and%20space?percent%20=hello%20world&plus+=hello+world&encoded%2B=1%2B2#frag+ment%20here"

            underTest.assertNextRequest {
                url {
                    url("https://ex+ample.com/path/with+plus/and space?percent =hello world&plus =hello world&encoded+=1+2#frag+ment here")
                }
            }
        }
    }
}

private suspend fun FunSpecContainerScope.pathTests(
    fixture: HttpApythiaTest.Fixture
) = with(fixture) {
    context("path") {
        test("failure") {
            underTest.actualRequestUrl = "http://example.com"

            shouldFail {
                underTest.assertNextRequest {
                    url { path("/path") }
                }
            }
        }

        test("success") {
            val expected = "/path/to/something"
            underTest.actualRequestUrl = "http://example.com$expected"

            shouldNotFail {
                underTest.assertNextRequest {
                    url { path(expected) }
                }
            }
        }

        test("encoded path success") {
            underTest.actualRequestUrl = "https://example.com/path/with+plus/and%20space"

            underTest.assertNextRequest {
                url { path("/path/with+plus/and space") }
            }
        }
    }
}

private suspend fun FunSpecContainerScope.pathSuffixTests(
    fixture: HttpApythiaTest.Fixture
) = with(fixture) {
    context("pathSuffix") {
        test("failure") {
            underTest.actualRequestUrl = "http://example.com/path/to/something"

            shouldFail {
                underTest.assertNextRequest {
                    url { pathSuffix("to/something-else") }
                }
            }
        }

        test("success") {
            val expected = "to/something"
            underTest.actualRequestUrl = "http://example.com/path/$expected"

            shouldNotFail {
                underTest.assertNextRequest {
                    url { pathSuffix(expected) }
                }
            }
        }

        test("encoded path suffix success") {
            underTest.actualRequestUrl = "https://ex+ample.com/path/with+plus/and%20space"

            underTest.assertNextRequest {
                url { pathSuffix("+plus/and space") }
            }
        }
    }
}
