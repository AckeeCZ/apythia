package io.github.ackeecz.apythia.http

/**
 * Charsets supported by [HttpApythia]
 */
public enum class Charset {

    UTF_8;

    public companion object {

        @Deprecated(
            level = DeprecationLevel.ERROR,
            message = "This method is for internal purposes only and should not be used.",
        )
        public fun checkCharsetSupported(contentType: String?) {
            contentType?.split(";")
                ?.map { it.trim() }
                ?.firstOrNull { it.startsWith("charset=", ignoreCase = true) }
                ?.substringAfter("charset=")
                ?.lowercase()
                ?.let {
                    if (it != "utf-8") {
                        throw UnsupportedEncodingException()
                    }
                }
        }
    }
}

public class UnsupportedEncodingException : RuntimeException("Only UTF-8 encoding is currently supported")
