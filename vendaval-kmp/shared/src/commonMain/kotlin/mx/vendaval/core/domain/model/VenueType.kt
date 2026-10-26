package mx.vendaval.core.domain.model

import mx.vendaval.core.domain.text.foldForSearch

/**
 * The kinds of venue the catalogue distinguishes.
 *
 * [label] is the exact Spanish string used by the original web dataset and by
 * the `type` column in Postgres, so it doubles as the wire format. Parsing is
 * tolerant ([fromLabel]) because the table is writable by the public and rows
 * arrive with inconsistent casing and accents.
 */
enum class VenueType(val label: String) {
    CASA_DE_CULTURA("Casa de Cultura"),
    CENTRO_CULTURAL("Centro Cultural"),
    GALERIA_DE_ARTE("Galería de Arte"),
    TEATRO("Teatro"),
    CENTRO_COMUNITARIO("Centro Comunitario"),

    /**
     * Escape hatch for rows whose type is unrecognised. Keeping them visible
     * beats dropping community contributions because of a typo.
     */
    OTRO("Otro"),
    ;

    /** Short form for dense chips where the full label would wrap. */
    val shortLabel: String
        get() = when (this) {
            CASA_DE_CULTURA -> "Casas"
            CENTRO_CULTURAL -> "Centros"
            GALERIA_DE_ARTE -> "Galerías"
            TEATRO -> "Teatros"
            CENTRO_COMUNITARIO -> "Comunitarios"
            OTRO -> "Otros"
        }

    companion object {
        /** Types offered in the filter bar and the submission form, in display order. */
        val selectable: List<VenueType> = listOf(
            CASA_DE_CULTURA,
            CENTRO_CULTURAL,
            GALERIA_DE_ARTE,
            TEATRO,
            CENTRO_COMUNITARIO,
        )

        private val byFoldedLabel: Map<String, VenueType> =
            entries.associateBy { it.label.foldForSearch() }

        /**
         * Resolves a wire value to a type, falling back to [OTRO]. Also accepts
         * the enum's own `name` so locally persisted data stays readable if the
         * storage format ever switches to the constant name.
         */
        fun fromLabel(raw: String?): VenueType {
            if (raw.isNullOrBlank()) return OTRO
            val folded = raw.foldForSearch()
            byFoldedLabel[folded]?.let { return it }
            entries.firstOrNull { it.name.equals(raw.trim(), ignoreCase = true) }?.let { return it }
            // Last resort: a row saying "Casa de la Cultura" should still land on
            // CASA_DE_CULTURA rather than being bucketed as unknown.
            return entries.firstOrNull { folded.contains(it.label.foldForSearch()) }
                ?: partialMatch(folded)
        }

        private fun partialMatch(folded: String): VenueType = when {
            folded.contains("casa") -> CASA_DE_CULTURA
            folded.contains("galeria") -> GALERIA_DE_ARTE
            folded.contains("teatro") || folded.contains("foro") -> TEATRO
            folded.contains("biblioteca") || folded.contains("centro cultural") -> CENTRO_CULTURAL
            folded.contains("comunitario") || folded.contains("crecer") -> CENTRO_COMUNITARIO
            else -> OTRO
        }
    }
}
