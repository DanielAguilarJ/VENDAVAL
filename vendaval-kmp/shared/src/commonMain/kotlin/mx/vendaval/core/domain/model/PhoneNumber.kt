package mx.vendaval.core.domain.model

import kotlin.jvm.JvmInline

/**
 * A dialable phone number.
 *
 * The source dataset encodes "no phone" as the literal string
 * `"No disponible"`, which would otherwise reach the UI as a fake call button.
 * [orNull] turns every such sentinel into a real `null`, so absence is
 * represented in the type system instead of in a magic string.
 */
@JvmInline
value class PhoneNumber private constructor(val display: String) {

    /** Digits only, with any `ext.` suffix dropped — extensions break `tel:`. */
    val dialDigits: String
        get() {
            val marker = display.indexOf(EXTENSION_MARKER, ignoreCase = true)
            val dialablePart = if (marker >= 0) display.substring(0, marker) else display
            return dialablePart.filter { it.isDigit() }
        }

    /**
     * `tel:` URI. Ten-digit national numbers are promoted to E.164 with the
     * Mexican country code so the link also works for a roaming user.
     */
    val dialUri: String
        get() {
            val digits = dialDigits
            return if (digits.length == 10) "tel:+52$digits" else "tel:$digits"
        }

    /** An extension the user has to punch in manually after the call connects. */
    val extension: String?
        get() {
            val marker = display.indexOf(EXTENSION_MARKER, ignoreCase = true)
            if (marker < 0) return null
            return display.substring(marker)
                .filter { it.isDigit() }
                .takeIf { it.isNotEmpty() }
        }

    override fun toString(): String = display

    companion object {
        private const val EXTENSION_MARKER = "ext"

        private val UNAVAILABLE = setOf(
            "no disponible", "no aplica", "sin telefono", "sin teléfono",
            "n/a", "na", "-", "--", "s/n", "null",
        )

        fun orNull(raw: String?): PhoneNumber? {
            val trimmed = raw?.trim().orEmpty()
            if (trimmed.isEmpty()) return null
            if (trimmed.lowercase() in UNAVAILABLE) return null
            // Needs enough digits to be a real Mexican landline or mobile.
            if (trimmed.count { it.isDigit() } < 7) return null
            return PhoneNumber(trimmed)
        }
    }
}
