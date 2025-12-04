package io.github.ackeecz.apythia.http.apythia.assertion

import io.github.ackeecz.apythia.http.apythia.HttpApythiaTest
import io.kotest.core.spec.style.scopes.FunSpecContainerScope

internal suspend fun FunSpecContainerScope.requestHeadersTests(
    fixture: HttpApythiaTest.Fixture,
) = with(fixture) {
    context("request headers") {
        headersTestSuite(
            fixture = fixture,
            arrangeHeaders = { underTest.actualRequestHeaders = it },
            assertHeaders = { headers(it) },
        )
    }
}
