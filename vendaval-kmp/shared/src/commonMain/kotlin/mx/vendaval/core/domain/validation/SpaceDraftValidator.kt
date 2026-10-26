package mx.vendaval.core.domain.validation

import mx.vendaval.core.domain.model.DraftField
import mx.vendaval.core.domain.model.DraftValidation
import mx.vendaval.core.domain.model.GeoPoint
import mx.vendaval.core.domain.model.SpaceDraft

/**
 * Validates community submissions before they reach the network.
 *
 * Written as a pure object with no dependencies so the entire rule set is
 * unit-testable and runs identically on every platform. This is the app's only
 * write path to a publicly writable table, so it is also the place where input
 * hygiene is enforced.
 */
object SpaceDraftValidator {

    /** Generous upper bounds that still stop obvious abuse of a public table. */
    private const val MAX_NAME = 120
    private const val MAX_ADDRESS = 200
    private const val MIN_NAME = 3

    /** Bounds for the optional community-profile fields. */
    private const val MAX_DESCRIPTION = 600
    private const val MAX_HOURS = 160
    private const val MAX_WEBSITE = 200
    private const val MAX_SOCIAL = 200

    /**
     * Rough envelope of the state of Aguascalientes, padded. Used only to raise
     * a warning, never to reject.
     */
    private val STATE_LAT = 21.55..22.55
    private val STATE_LON = -102.95..-101.75

    /** Shared between the hard-error and warning paths so the wording cannot drift. */
    const val SWAPPED_MESSAGE = "¿Invertiste latitud y longitud?"

    operator fun invoke(draft: SpaceDraft): DraftValidation {
        val errors = mutableMapOf<DraftField, String>()
        val warnings = mutableMapOf<DraftField, String>()

        val name = draft.name.trim()
        when {
            name.isEmpty() -> errors[DraftField.NAME] = "El nombre es obligatorio"
            name.length < MIN_NAME -> errors[DraftField.NAME] = "Usa al menos $MIN_NAME caracteres"
            name.length > MAX_NAME -> errors[DraftField.NAME] = "Máximo $MAX_NAME caracteres"
            containsMarkup(name) -> errors[DraftField.NAME] = "No se permiten etiquetas HTML"
        }

        val address = draft.address.trim()
        when {
            address.isEmpty() -> errors[DraftField.ADDRESS] = "La dirección es obligatoria"
            address.length > MAX_ADDRESS -> errors[DraftField.ADDRESS] = "Máximo $MAX_ADDRESS caracteres"
            containsMarkup(address) -> errors[DraftField.ADDRESS] = "No se permiten etiquetas HTML"
        }

        if (draft.type == null) errors[DraftField.TYPE] = "Elige un tipo de recinto"
        if (draft.municipality == null) errors[DraftField.MUNICIPALITY] = "Elige un municipio"

        // Phone is optional, but if provided it has to be dialable.
        val phone = draft.phone.trim()
        if (phone.isNotEmpty() && phone.count { it.isDigit() } < 7) {
            errors[DraftField.PHONE] = "Ingresa un teléfono de al menos 7 dígitos o déjalo vacío"
        }

        val latitude = draft.latitude.trim().toDoubleOrNull()
        val longitude = draft.longitude.trim().toDoubleOrNull()

        if (draft.latitude.isBlank()) {
            errors[DraftField.LATITUDE] = "La latitud es obligatoria"
        } else if (latitude == null) {
            errors[DraftField.LATITUDE] = "Debe ser un número decimal"
        } else if (latitude !in -90.0..90.0) {
            // A latitude that is out of range but would be a legal longitude, paired with
            // a longitude that would be a legal latitude, is a swapped pair. Saying so is
            // far more useful than restating the valid range.
            val looksSwapped = latitude in -180.0..180.0 &&
                longitude != null &&
                longitude in -90.0..90.0
            errors[DraftField.LATITUDE] = if (looksSwapped) {
                SWAPPED_MESSAGE
            } else {
                "Debe estar entre -90 y 90"
            }
        }

        if (draft.longitude.isBlank()) {
            errors[DraftField.LONGITUDE] = "La longitud es obligatoria"
        } else if (longitude == null) {
            errors[DraftField.LONGITUDE] = "Debe ser un número decimal"
        } else if (longitude !in -180.0..180.0) {
            errors[DraftField.LONGITUDE] = "Debe estar entre -180 y 180"
        }

        if (latitude != null && longitude != null &&
            latitude in -90.0..90.0 && longitude in -180.0..180.0
        ) {
            // A very common paste error is swapping the two fields. In this
            // region longitude is negative and latitude positive, so a negative
            // latitude paired with a positive longitude is almost certainly a swap.
            if (latitude < 0 && longitude > 0) {
                warnings[DraftField.LATITUDE] = SWAPPED_MESSAGE
            } else if (latitude !in STATE_LAT || longitude !in STATE_LON) {
                warnings[DraftField.LATITUDE] =
                    "Estas coordenadas caen fuera de Aguascalientes"
            }
        }

        // Optional community-profile fields. Only length + markup hygiene, since a
        // public catalogue must not store either an essay or an HTML tag; a blank
        // field is always fine because none of these are required.
        val description = draft.description.trim()
        if (description.length > MAX_DESCRIPTION) {
            errors[DraftField.DESCRIPTION] = "Máximo $MAX_DESCRIPTION caracteres"
        } else if (containsMarkup(description)) {
            errors[DraftField.DESCRIPTION] = "No se permiten etiquetas HTML"
        }

        val hours = draft.hours.trim()
        if (hours.length > MAX_HOURS) {
            errors[DraftField.HOURS] = "Máximo $MAX_HOURS caracteres"
        } else if (containsMarkup(hours)) {
            errors[DraftField.HOURS] = "No se permiten etiquetas HTML"
        }

        val website = draft.website.trim()
        if (website.isNotEmpty()) {
            when {
                website.length > MAX_WEBSITE ->
                    errors[DraftField.WEBSITE] = "Máximo $MAX_WEBSITE caracteres"
                containsMarkup(website) ->
                    errors[DraftField.WEBSITE] = "No se permiten etiquetas HTML"
                !looksLikeUrl(website) ->
                    errors[DraftField.WEBSITE] = "Ingresa una dirección válida (ej. https://…)"
            }
        }

        // One field per platform in the form. Report the first invalid handle under a
        // single key so the form can surface it beside the social section.
        for ((platform, raw) in draft.socialLinks) {
            val value = raw.trim()
            if (value.isEmpty()) continue
            val message = when {
                value.length > MAX_SOCIAL -> "Máximo $MAX_SOCIAL caracteres"
                containsMarkup(value) -> "No se permiten etiquetas HTML"
                value.any { it.isWhitespace() } -> "No debe contener espacios"
                else -> null
            }
            if (message != null) {
                errors[DraftField.SOCIAL] = "${platform.label}: $message"
                break
            }
        }

        return DraftValidation(errors = errors, warnings = warnings)
    }

    /** Cheap guard against markup being stored in a shared, public catalogue. */
    private fun containsMarkup(value: String): Boolean =
        value.contains('<') || value.contains('>')

    /**
     * Lenient URL sanity check. Accepts a bare host ("vendaval.mx") by assuming https,
     * and only requires a dotted host with no whitespace — enough to catch a stray word
     * without rejecting the shapes people actually paste.
     */
    private fun looksLikeUrl(value: String): Boolean {
        val trimmed = value.trim()
        if (trimmed.isEmpty() || trimmed.any { it.isWhitespace() }) return false
        val withScheme = if (Regex("^https?://", RegexOption.IGNORE_CASE).containsMatchIn(trimmed)) {
            trimmed
        } else {
            "https://$trimmed"
        }
        val host = withScheme.substringAfter("://").substringBefore('/').substringBefore('?')
        return host.length >= 3 && host.contains('.')
    }

    /**
     * Builds the canonical map query the original site stored alongside each
     * record, so entries created here behave identically on the web portal.
     */
    fun deriveMapQuery(draft: SpaceDraft): String {
        val explicit = draft.mapQuery.trim()
        if (explicit.isNotEmpty()) return explicit
        return listOf(draft.name.trim(), draft.municipality?.label.orEmpty(), "Aguascalientes")
            .filter { it.isNotEmpty() }
            .joinToString(" ")
            .replace(Regex("\\s+"), "+")
    }

    /** The parsed coordinate, or `null` when validation failed. */
    fun coordinate(draft: SpaceDraft): GeoPoint? =
        GeoPoint.orNull(draft.latitude, draft.longitude)
}
