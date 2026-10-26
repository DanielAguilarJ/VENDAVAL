package mx.vendaval.core.domain.model

/**
 * Unvalidated user input from the "add a venue" form.
 *
 * Coordinates stay as `String` on purpose: a half-typed `-102.` must be
 * representable without losing the user's keystrokes, and the field-level error
 * has to survive recomposition. Parsing happens once, in [SpaceDraftValidator].
 */
data class SpaceDraft(
    val name: String = "",
    val address: String = "",
    val phone: String = "",
    val type: VenueType? = null,
    val municipality: Municipality? = null,
    val latitude: String = "",
    val longitude: String = "",
    val mapQuery: String = "",
    val description: String = "",
    val hours: String = "",
    val website: String = "",
    val socialLinks: Map<SocialPlatform, String> = emptyMap(),
) {
    val hasContent: Boolean
        get() = listOf(name, address, phone, latitude, longitude, mapQuery, description, hours, website)
            .any { it.isNotBlank() } ||
            type != null || municipality != null ||
            socialLinks.values.any { it.isNotBlank() }
}

/** Every field that can carry a validation message. */
enum class DraftField {
    NAME,
    ADDRESS,
    PHONE,
    TYPE,
    MUNICIPALITY,
    LATITUDE,
    LONGITUDE,
    DESCRIPTION,
    HOURS,
    WEBSITE,
    SOCIAL,
}

/**
 * Outcome of validating a [SpaceDraft].
 *
 * Errors block submission; warnings do not. The distinction exists because a
 * venue whose coordinates fall outside the state is *probably* a mistake but
 * might be a legitimate edge case, and refusing it outright would silently lose
 * a community contribution.
 */
data class DraftValidation(
    val errors: Map<DraftField, String> = emptyMap(),
    val warnings: Map<DraftField, String> = emptyMap(),
) {
    val isValid: Boolean get() = errors.isEmpty()
}
