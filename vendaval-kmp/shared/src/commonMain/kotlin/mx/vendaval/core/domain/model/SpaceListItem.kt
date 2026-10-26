package mx.vendaval.core.domain.model

/**
 * A catalogue entry decorated with the per-user, per-session state the list and
 * map need to render a row without reaching back into repositories.
 *
 * Keeping this separate from [CulturalSpace] means favourite toggles and
 * location updates never mutate the catalogue itself, and the projection is
 * cheap to recompute.
 */
data class SpaceListItem(
    val space: CulturalSpace,
    val isFavourite: Boolean = false,
    val distanceKm: Double? = null,
) {
    val id: SpaceId get() = space.id
}

/**
 * Aggregate coverage figures for the statistics screen. Replaces the three
 * hard-coded counters on the original site, which had drifted out of sync with
 * the dataset (the hero advertised 20 venues while the array held 27).
 */
data class CoverageStats(
    val totalSpaces: Int,
    val byType: Map<VenueType, Int>,
    val byMunicipality: Map<Municipality, Int>,
    val municipalitiesCovered: Int,
    val municipalitiesInState: Int,
    val withPhone: Int,
    val favourites: Int,
    val localContributions: Int,
) {
    val distinctTypes: Int get() = byType.count { it.value > 0 }

    val phoneCoverageRatio: Float
        get() = if (totalSpaces == 0) 0f else withPhone.toFloat() / totalSpaces

    val municipalityCoverageRatio: Float
        get() = if (municipalitiesInState == 0) 0f
        else municipalitiesCovered.toFloat() / municipalitiesInState

    companion object {
        val EMPTY = CoverageStats(
            totalSpaces = 0,
            byType = emptyMap(),
            byMunicipality = emptyMap(),
            municipalitiesCovered = 0,
            municipalitiesInState = Municipality.entries.size,
            withPhone = 0,
            favourites = 0,
            localContributions = 0,
        )
    }
}
