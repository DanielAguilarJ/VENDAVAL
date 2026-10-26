package mx.vendaval.core.domain.usecase

import mx.vendaval.core.domain.geo.distanceKm
import mx.vendaval.core.domain.model.CulturalSpace
import mx.vendaval.core.domain.model.GeoPoint
import mx.vendaval.core.domain.model.SortMode
import mx.vendaval.core.domain.model.SpaceFilter
import mx.vendaval.core.domain.model.SpaceId
import mx.vendaval.core.domain.model.SpaceListItem
import mx.vendaval.core.domain.text.foldForSearch

/**
 * Turns the catalogue plus the current query state into the exact list the UI
 * renders.
 *
 * Deliberately a pure function of its inputs: no repositories, no coroutines, no
 * platform types. That makes the app's most behaviour-dense logic — search,
 * filtering, distance ordering — fully unit-testable, and lets the caller decide
 * which dispatcher to run it on.
 */
object SpaceQueryEngine {

    operator fun invoke(
        spaces: List<CulturalSpace>,
        favourites: Set<SpaceId>,
        filter: SpaceFilter,
        sort: SortMode,
        reference: GeoPoint?,
    ): List<SpaceListItem> {
        // Fold the needle once rather than once per row.
        val needle = filter.query.trim().foldForSearch()

        val matched = spaces.asSequence()
            .filter { filter.types.isEmpty() || it.type in filter.types }
            .filter { filter.municipalities.isEmpty() || it.municipality in filter.municipalities }
            .filter { !filter.favouritesOnly || it.id in favourites }
            .filter { needle.isEmpty() || it.searchIndex.contains(needle) }
            .map { space ->
                SpaceListItem(
                    space = space,
                    isFavourite = space.id in favourites,
                    distanceKm = reference?.let { distanceKm(it, space.location) },
                )
            }
            .toList()

        return matched.sortedWith(comparatorFor(sort, reference))
    }

    /**
     * Ordering rules. Every comparator ends with a name tiebreaker so results
     * are deterministic — without it, equal-distance or same-type rows would
     * reshuffle between recompositions and the list would appear to flicker.
     */
    private fun comparatorFor(
        sort: SortMode,
        reference: GeoPoint?,
    ): Comparator<SpaceListItem> {
        val byName = compareBy<SpaceListItem> { it.space.name.foldForSearch() }

        return when (sort) {
            SortMode.NAME -> byName

            // Falls back to alphabetical when there is no reference point,
            // rather than presenting an arbitrary order as "nearest".
            SortMode.DISTANCE -> if (reference == null) {
                byName
            } else {
                compareBy<SpaceListItem> { it.distanceKm ?: Double.MAX_VALUE }.then(byName)
            }

            SortMode.TYPE -> compareBy<SpaceListItem> { it.space.type.ordinal }.then(byName)

            SortMode.MUNICIPALITY ->
                compareBy<SpaceListItem> { it.space.municipality.label.foldForSearch() }.then(byName)
        }
    }
}
