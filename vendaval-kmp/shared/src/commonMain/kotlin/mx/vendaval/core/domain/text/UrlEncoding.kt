package mx.vendaval.core.domain.text

/**
 * Minimal percent-encoder for query components.
 *
 * `java.net.URLEncoder` is JVM-only and `NSString.stringByAddingPercentEncoding`
 * is Apple-only, so building external map links in common code needs its own
 * encoder. Only unreserved characters per RFC 3986 survive unescaped; spaces
 * become `%20` rather than `+` because the result is also used in path
 * segments where `+` is literal.
 */
fun String.percentEncode(): String {
    val out = StringBuilder(length + 16)
    for (byte in encodeToByteArray()) {
        val value = byte.toInt() and 0xFF
        val ch = value.toChar()
        val unreserved = ch in 'A'..'Z' || ch in 'a'..'z' || ch in '0'..'9' ||
            ch == '-' || ch == '_' || ch == '.' || ch == '~'
        if (unreserved) {
            out.append(ch)
        } else {
            out.append('%')
            out.append(HEX[value shr 4])
            out.append(HEX[value and 0x0F])
        }
    }
    return out.toString()
}

private const val HEX = "0123456789ABCDEF"
