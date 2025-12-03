package io.github.ackeecz.apythia.http.util

/**
 * Helper class to wrap a [byteArray] for overriding equals/hash code on one place and avoiding to
 * do this in every data class which uses [ByteArray]
 */
internal data class ByteArrayWrapper(val byteArray: ByteArray) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ByteArrayWrapper) return false

        if (!byteArray.contentEquals(other.byteArray)) return false

        return true
    }

    override fun hashCode(): Int {
        return byteArray.contentHashCode()
    }

    override fun toString(): String = byteArray.toHexString()
}

internal fun ByteArray.wrap(): ByteArrayWrapper = ByteArrayWrapper(this)
