package mx.vendaval.core.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import mx.vendaval.core.domain.model.CulturalSpace
import mx.vendaval.core.domain.model.SpaceDraft
import mx.vendaval.core.domain.model.SpaceId
import mx.vendaval.core.domain.model.SyncState

/**
 * Single source of truth for the venue catalogue.
 *
 * The contract is offline-first by construction: [spaces] always emits usable
 * data (seeded at minimum) and never waits on the network, while [syncState]
 * separately reports whether the mirror is fresh. A failed refresh degrades the
 * badge, never the list.
 */
interface SpaceRepository {

    /** Merged view of seeded, remotely fetched and locally created venues. */
    val spaces: Flow<List<CulturalSpace>>

    val syncState: StateFlow<SyncState>

    /** Reads the catalogue once from disk into memory. Idempotent. */
    suspend fun warmUp()

    /**
     * Pulls the remote catalogue and merges it into the cache.
     * Never throws: transport problems surface through [syncState].
     */
    suspend fun refresh()

    /**
     * Persists a validated submission locally, then attempts to publish it.
     *
     * Returns immediately after the local write so the UI can close the form —
     * the record is already visible and durable. Publication continues in the
     * background and flips the record's origin once acknowledged.
     */
    suspend fun submit(draft: SpaceDraft): Result<CulturalSpace>

    /** Retries publication of every locally created record. */
    suspend fun retryPendingSubmissions()

    /** Removes a locally created record. Seeded and remote records are immutable here. */
    suspend fun deleteLocal(id: SpaceId): Boolean

    /**
     * Live inserts from other clients, mirroring the web portal's realtime
     * subscription. Collecting is optional; the app is fully functional without it.
     */
    fun realtimeInserts(): Flow<CulturalSpace>
}

/** Per-device favourites. Never leaves the device. */
interface FavouritesRepository {
    val favourites: Flow<Set<SpaceId>>
    suspend fun toggle(id: SpaceId)
    suspend fun isFavourite(id: SpaceId): Boolean
}
