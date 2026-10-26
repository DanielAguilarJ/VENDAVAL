package mx.vendaval.core.domain.model

import mx.vendaval.core.domain.text.foldForSearch

/**
 * The eleven municipalities of the state of Aguascalientes.
 *
 * The seeded catalogue only covers seven of them; the remaining four are
 * modelled anyway so the submission form can accept contributions from
 * anywhere in the state and the coverage view can show real gaps instead of
 * silently pretending they do not exist.
 */
enum class Municipality(val label: String, val approximateCentre: GeoPoint) {
    AGUASCALIENTES("Aguascalientes", GeoPoint(21.8818, -102.2916)),
    ASIENTOS("Asientos", GeoPoint(22.2378, -102.0897)),
    CALVILLO("Calvillo", GeoPoint(21.8465, -102.7188)),
    COSIO("Cosío", GeoPoint(22.3661, -102.3005)),
    EL_LLANO("El Llano", GeoPoint(21.8686, -101.9739)),
    JESUS_MARIA("Jesús María", GeoPoint(21.9612, -102.3435)),
    PABELLON_DE_ARTEAGA("Pabellón de Arteaga", GeoPoint(22.1415, -102.2762)),
    RINCON_DE_ROMOS("Rincón de Romos", GeoPoint(22.2289, -102.3241)),
    SAN_FRANCISCO_DE_LOS_ROMO("San Francisco de los Romo", GeoPoint(22.0738, -102.2710)),
    SAN_JOSE_DE_GRACIA("San José de Gracia", GeoPoint(22.1506, -102.4158)),
    TEPEZALA("Tepezalá", GeoPoint(22.2200, -102.1700)),
    ;

    companion object {
        private val byFoldedLabel: Map<String, Municipality> =
            entries.associateBy { it.label.foldForSearch() }

        /**
         * Resolves a wire value. Unlike [VenueType] there is no catch-all
         * member, so an unrecognised municipality yields `null` and the caller
         * decides whether to drop the row or attribute it to the capital.
         */
        fun fromLabel(raw: String?): Municipality? {
            if (raw.isNullOrBlank()) return null
            val folded = raw.foldForSearch()
            byFoldedLabel[folded]?.let { return it }
            entries.firstOrNull { it.name.equals(raw.trim(), ignoreCase = true) }?.let { return it }
            return entries.firstOrNull { folded.contains(it.label.foldForSearch()) }
        }
    }
}
