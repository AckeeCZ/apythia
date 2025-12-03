package io.github.ackeecz.apythia.http

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.scopes.FunSpecContainerScope

internal suspend fun FunSpecContainerScope.callOnceTest(
    act: () -> Unit,
) {
    test("can be called only once") {
        act()

        shouldThrow<IllegalStateException> {
            act()
        }
    }
}
