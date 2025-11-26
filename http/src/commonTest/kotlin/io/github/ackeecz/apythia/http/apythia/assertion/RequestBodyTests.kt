package io.github.ackeecz.apythia.http.apythia.assertion

import io.github.ackeecz.apythia.http.apythia.HttpApythiaTest
import io.kotest.core.spec.style.scopes.FunSpecContainerScope

internal suspend fun FunSpecContainerScope.requestBodyTests(
    fixture: HttpApythiaTest.Fixture,
) = with(fixture) {
    context("request body") {
        bodyTestSuite(
            fixture = fixture,
            arrangeHeaders = { underTest.actualRequestHeaders = it },
            arrangeBody = { underTest.actualRequestBody = it },
            assertBody = { body(it) },
        )
        // We don't include multipart tests to bodyTestSuite because they are called from
        // multipart tests as well and there would be recursion. Also it would make test setup harder.
        multipartFormDataBodyTests(fixture)
        partialMultipartFormDataBodyTests(fixture)
    }
}
