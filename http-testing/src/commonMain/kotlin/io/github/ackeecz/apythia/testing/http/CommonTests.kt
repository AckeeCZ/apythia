package io.github.ackeecz.apythia.testing.http

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.scopes.FunSpecContainerScope

public suspend fun FunSpecContainerScope.callOnceTest(
    act: () -> Unit,
) {
    test("can be called only once") {
        act()

        shouldThrow<IllegalStateException> {
            act()
        }
    }
}
