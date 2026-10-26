package mx.vendaval.core.domain.model

/**
 * The complete query state of the directory.
 *
 * Modelled as one immutable value so the whole search state can be snapshotted,
 * restored, compared for equality (which lets the UI skip recomposition) and
 * unit-tested without a UI.
 *
 * Empty selection sets mean "no constraint" rather than "match nothing", which
 * matches the original site's "Todos" chip and avoids an empty-state trap on
 * first launch.
 */
data class SpaceFilter(
    val query: String = "",
    val types: Set<VenueType> = emptySet(),
    val municipalities: Set<Municipality> = emptySet(),
    val favouritesOnly: Boolean = false,
) {
    val isActive: Boolean
        get() = query.isNotBlank() || types.isNotEmpty() ||
            municipalities.isNotEmpty() || favouritesOnly

    /** Count shown on the "clear filters" affordance. */
    val activeConstraintCount: Int
        get() = (if (query.isNotBlank()) 1 else 0) +
            types.size + municipalities.size + (if (favouritesOnly) 1 else 0)

    fun toggleType(type: VenueType): SpaceFilter =
        copy(types = types.toggled(type))

    fun toggleMunicipality(municipality: Municipality): SpaceFilter =
        copy(municipalities = municipalities.toggled(municipality))

    fun cleared(): SpaceFilter = SpaceFilter()
}

private fun <T> Set<T>.toggled(item: T): Set<T> =
    if (item in this) this - item else this + item

/** Ordering options offered in the directory toolbar. */
enum class SortMode(val label: String) {
    /** Alphabetical, accent-aware. The default because it is stable and cheap. */
    NAME("Nombre"),

    /**
     * Nearest first. Only selectable once a reference location exists; the
     * use case falls back to [NAME] rather than producing an arbitrary order.
     */
    DISTANCE("Cercanía"),

    TYPE("Recinto"),
    MUNICIPALITY("Municipio"),
    ;

    companion object {
        val default = NAME
    }
}
