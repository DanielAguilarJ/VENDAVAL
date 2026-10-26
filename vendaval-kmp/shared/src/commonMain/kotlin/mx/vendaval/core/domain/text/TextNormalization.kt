package mx.vendaval.core.domain.text

/**
 * Accent- and case-insensitive folding for search and enum parsing.
 *
 * `java.text.Normalizer` is JVM-only and Foundation's folding is Apple-only, so
 * a hand-rolled table keeps the behaviour identical on all three targets. That
 * matters here: a user typing "galeria" must match "Galería de Arte", and a
 * remote row spelling a venue type without accents must still resolve.
 *
 * The table covers the Spanish diacritics that actually occur in the corpus
 * plus the wider Latin-1 range so imported rows never fall through.
 */
private val FOLDING: Map<Char, Char> = buildMap {
    "àáâãäåāăą".forEach { put(it, 'a') }
    "èéêëēĕėęě".forEach { put(it, 'e') }
    "ìíîïĩīĭįı".forEach { put(it, 'i') }
    "òóôõöøōŏő".forEach { put(it, 'o') }
    "ùúûüũūŭůų".forEach { put(it, 'u') }
    "ñń".forEach { put(it, 'n') }
    "çćĉċ".forEach { put(it, 'c') }
    "ýÿ".forEach { put(it, 'y') }
}

/**
 * Lowercases, strips diacritics and collapses whitespace runs to a single
 * space. Punctuation is preserved because venue names use it meaningfully
 * (for example `Artefacto / Espacio Experimental`).
 */
fun String.foldForSearch(): String {
    val builder = StringBuilder(length)
    var lastWasSpace = false
    for (raw in lowercase()) {
        val ch = FOLDING[raw] ?: raw
        if (ch.isWhitespace()) {
            if (!lastWasSpace && builder.isNotEmpty()) builder.append(' ')
            lastWasSpace = true
        } else {
            builder.append(ch)
            lastWasSpace = false
        }
    }
    return builder.toString().trimEnd()
}

/**
 * Folded substring match. Returns `true` for a blank needle so callers can pass
 * an empty search box straight through without branching.
 */
fun String.matchesFolded(needle: String): Boolean {
    if (needle.isBlank()) return true
    return foldForSearch().contains(needle.foldForSearch())
}
