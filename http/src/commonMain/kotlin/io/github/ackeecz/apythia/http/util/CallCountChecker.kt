package io.github.ackeecz.apythia.http.util

internal class CallCountChecker(
    private val actionName: String,
    private val maxCallCount: Int = 1,
) {

    private var callCount = 0

    fun incrementOrFail() {
        callCount++
        if (callCount > maxCallCount) {
            error("$actionName can't be called more than $maxCallCount times")
        }
    }
}
