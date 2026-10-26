package mx.vendaval.app.presentation

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import mx.vendaval.core.concurrent.ioDispatcher
import mx.vendaval.core.data.repository.FavouritesRepositoryImpl
import mx.vendaval.core.domain.model.CoverageStats
import mx.vendaval.core.domain.model.CulturalSpace
import mx.vendaval.core.domain.model.GeoPoint
import mx.vendaval.core.domain.model.Municipality
import mx.vendaval.core.domain.model.SortMode
import mx.vendaval.core.domain.model.SpaceFilter
import mx.vendaval.core.domain.model.SpaceId
import mx.vendaval.core.domain.model.SpaceListItem
import mx.vendaval.core.domain.model.SyncState
import mx.vendaval.core.domain.model.VenueType
import mx.vendaval.core.domain.repository.SpaceRepository
import mx.vendaval.core.domain.service.LocationOutcome
import mx.vendaval.core.domain.service.LocationProvider
import mx.vendaval.core.domain.usecase.ComputeCoverageStats
import mx.vendaval.core.domain.usecase.SpaceQueryEngine

/** Result of the last "where am I" attempt, surfaced so the UI can explain itself. */
sealed interface LocationStatus {
    data object Unknown : LocationStatus
    data object Requesting : LocationStatus
    data class Resolved(val point: GeoPoint, val label: String?) : LocationStatus
    data class Rejected(val outcome: LocationOutcome) : LocationStatus
}

/**
 * App-scoped state for everything derived from the catalogue.
 *
 * The directory, the map and the favourites tab are three presentations of the
 * *same* query, so they share one holder instead of each owning a copy of the
 * filter. That is cohesion, not a god object: the class exposes one dataset and the
 * operations that narrow it, and delegates every actual rule to the domain
 * ([SpaceQueryEngine], [ComputeCoverageStats]) so it stays thin.
 */
class CatalogueStateHolder(
    private val spaceRepository: SpaceRepository,
    private val favouritesRepository: FavouritesRepositoryImpl,
    private val locationProvider: LocationProvider,
) : StateHolder() {

    private val _filter = MutableStateFlow(SpaceFilter())
    val filter: StateFlow<SpaceFilter> = _filter.asStateFlow()

    private val _sortMode = MutableStateFlow(SortMode.default)
    val sortMode: StateFlow<SortMode> = _sortMode.asStateFlow()

    private val _locationStatus = MutableStateFlow<LocationStatus>(LocationStatus.Unknown)
    val locationStatus: StateFlow<LocationStatus> = _locationStatus.asStateFlow()

    /** Newly inserted venue from the realtime feed, shown as a transient banner. */
    private val _liveInsert = MutableStateFlow<CulturalSpace?>(null)
    val liveInsert: StateFlow<CulturalSpace?> = _liveInsert.asStateFlow()

    val syncState: StateFlow<SyncState> = spaceRepository.syncState

    val allSpaces: StateFlow<List<CulturalSpace>> =
        spaceRepository.spaces.stateIn(scope, SharingStarted.Eagerly, emptyList())

    val favourites: StateFlow<Set<SpaceId>> = favouritesRepository.favourites

    val locationPoint: GeoPoint?
        get() = (_locationStatus.value as? LocationStatus.Resolved)?.point

    /**
     * The list the UI renders.
     *
     * The query text is debounced so a fast typist does not trigger a full
     * re-filter per keystroke, while chip and sort changes are not debounced
     * because those must feel instant. Filtering runs on [ioDispatcher] to keep
     * the frame budget free on the main thread.
     */
    val visibleItems: StateFlow<List<SpaceListItem>> =
        combine(
            allSpaces,
            favouritesRepository.favourites,
            _filter.debounceQueryOnly(),
            _sortMode,
            _locationStatus,
        ) { spaces, favourites, filter, sort, location ->
            SpaceQueryEngine(
                spaces = spaces,
                favourites = favourites,
                filter = filter,
                sort = sort,
                reference = (location as? LocationStatus.Resolved)?.point,
            )
        }
            .distinctUntilChanged()
            .stateIn(scope, SharingStarted.Eagerly, emptyList())

    val coverage: StateFlow<CoverageStats> =
        combine(allSpaces, favouritesRepository.favourites) { spaces, favourites ->
            ComputeCoverageStats(spaces, favourites)
        }.stateIn(scope, SharingStarted.Lazily, CoverageStats.EMPTY)

    /** True once the platform can actually answer a location query. */
    val supportsLocation: Boolean get() = locationProvider.isSupported

    init {
        scope.launch {
            favouritesRepository.warmUp()
            spaceRepository.warmUp()
            spaceRepository.refresh()
        }

        // Realtime is an enhancement: collected in its own coroutine so a socket
        // failure can never take down the catalogue collectors above.
        scope.launch {
            try {
                spaceRepository.realtimeInserts().collect { inserted ->
                    _liveInsert.value = inserted
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Throwable) {
                // Silently give up on live updates; refresh still works.
            }
        }
    }

    fun findSpace(id: SpaceId?): CulturalSpace? =
        id?.let { wanted -> allSpaces.value.firstOrNull { it.id == wanted } }

    fun onQueryChange(query: String) {
        _filter.value = _filter.value.copy(query = query)
    }

    fun onToggleType(type: VenueType) {
        _filter.value = _filter.value.toggleType(type)
    }

    fun onToggleMunicipality(municipality: Municipality) {
        _filter.value = _filter.value.toggleMunicipality(municipality)
    }

    fun onClearTypes() {
        _filter.value = _filter.value.copy(types = emptySet())
    }

    fun onClearMunicipalities() {
        _filter.value = _filter.value.copy(municipalities = emptySet())
    }

    fun onToggleFavouritesOnly() {
        _filter.value = _filter.value.copy(favouritesOnly = !_filter.value.favouritesOnly)
    }

    fun onClearFilters() {
        _filter.value = _filter.value.cleared()
    }

    fun onSortChange(mode: SortMode) {
        _sortMode.value = mode
        // Asking to sort by proximity is an implicit request for a location.
        if (mode == SortMode.DISTANCE && locationPoint == null) requestLocation()
    }

    fun onToggleFavourite(id: SpaceId) {
        scope.launch { favouritesRepository.toggle(id) }
    }

    fun onRefresh() {
        scope.launch { spaceRepository.refresh() }
    }

    fun onDeleteLocal(id: SpaceId) {
        scope.launch { spaceRepository.deleteLocal(id) }
    }

    fun dismissLiveInsert() {
        _liveInsert.value = null
    }

    /** Uses a municipal seat as the distance reference where there is no GPS. */
    fun useMunicipalityAsReference(municipality: Municipality) {
        _locationStatus.value = LocationStatus.Resolved(
            point = municipality.approximateCentre,
            label = municipality.label,
        )
    }

    fun requestLocation() {
        scope.launch {
            _locationStatus.value = LocationStatus.Requesting
            _locationStatus.value = when (val outcome = locationProvider.currentLocation()) {
                is LocationOutcome.Available -> LocationStatus.Resolved(outcome.point, null)
                else -> LocationStatus.Rejected(outcome)
            }
        }
    }
}

/**
 * Debounces only the text field.
 *
 * A blanket `debounce` on the whole filter would also lag chip taps, which must
 * feel immediate. Comparing everything *except* the query lets a structural change
 * pass through instantly while consecutive keystrokes coalesce.
 *
 * `debounce` with a per-element timeout is still `@FlowPreview`; the opt-in is scoped
 * to this one function rather than the module so the exposure stays visible.
 */
@OptIn(kotlinx.coroutines.FlowPreview::class)
private fun StateFlow<SpaceFilter>.debounceQueryOnly() =
    this
        .map { it }
        .distinctUntilChanged()
        .debounce { filter -> if (filter.query.isEmpty()) 0L else SEARCH_DEBOUNCE_MS }

private const val SEARCH_DEBOUNCE_MS = 140L
