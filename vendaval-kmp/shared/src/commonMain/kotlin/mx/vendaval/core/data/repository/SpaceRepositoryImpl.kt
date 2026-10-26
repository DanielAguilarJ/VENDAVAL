package mx.vendaval.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import mx.vendaval.core.data.dto.SpaceDto
import mx.vendaval.core.data.dto.toDomainOrNull
import mx.vendaval.core.data.dto.toDto
import mx.vendaval.core.data.dto.toInsertDto
import mx.vendaval.core.data.local.CatalogueSnapshot
import mx.vendaval.core.data.local.CatalogueCache
import mx.vendaval.core.data.remote.ApiResult
import mx.vendaval.core.data.remote.SupabaseRealtimeSource
import mx.vendaval.core.data.remote.SupabaseSpaceApi
import mx.vendaval.core.data.seed.SeedCatalogue
import mx.vendaval.core.domain.model.CulturalSpace
import mx.vendaval.core.domain.model.SpaceDraft
import mx.vendaval.core.domain.model.SpaceId
import mx.vendaval.core.domain.model.SpaceOrigin
import mx.vendaval.core.domain.model.SyncFailure
import mx.vendaval.core.domain.model.SyncState
import mx.vendaval.core.domain.model.VenueType
import mx.vendaval.core.domain.repository.SpaceRepository
import mx.vendaval.core.domain.validation.SpaceDraftValidator

/**
 * Offline-first catalogue.
 *
 * ### Layering
 * Three sources are merged, in increasing order of authority:
 *  1. [SeedCatalogue] — compiled in, always present, so the very first launch on
 *     a plane is still a working app.
 *  2. the persisted mirror of the Postgres table;
 *  3. records created on this device that the server has not accepted yet.
 *
 * A remote row with the same id as a seeded one wins, because the server is the
 * shared truth for shared data. Local records live in a negative id space and can
 * therefore never be shadowed by, or shadow, anything from the server.
 *
 * ### Failure posture
 * [refresh] never throws and never empties the list. A failed sync downgrades
 * [syncState] while the cached catalogue stays on screen — the entire point of
 * the design, since the audience for this app includes places with poor coverage.
 */
class SpaceRepositoryImpl(
    private val api: SupabaseSpaceApi,
    private val realtimeSource: SupabaseRealtimeSource,
    private val cache: CatalogueCache,
    private val clock: Clock = Clock.System,
) : SpaceRepository {

    private val _spaces = MutableStateFlow(SeedCatalogue.spaces)
    override val spaces: StateFlow<List<CulturalSpace>> = _spaces.asStateFlow()

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    override val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    /** Authoritative persisted state; [_spaces] is its projection onto the domain. */
    private var snapshot: CatalogueSnapshot = CatalogueSnapshot.EMPTY

    private var warmedUp = false

    override suspend fun warmUp() {
        if (warmedUp) return
        warmedUp = true
        applySnapshot(cache.read(), persist = false)
    }

    override suspend fun refresh() {
        _syncState.value = SyncState.Syncing

        when (val result = api.fetchAll()) {
            is ApiResult.Success -> {
                applySnapshot(snapshot.copy(remote = result.value), persist = true)
                _syncState.value = SyncState.Synced(
                    at = clock.now(),
                    remoteCount = result.value.size,
                )
                // Piggyback on a successful connection to drain the outbox.
                retryPendingSubmissions()
            }

            is ApiResult.Failure -> {
                _syncState.value = SyncState.Failed(cause = result.cause, at = clock.now())
            }
        }
    }

    override suspend fun submit(draft: SpaceDraft): Result<CulturalSpace> {
        val validation = SpaceDraftValidator(draft)
        if (!validation.isValid) {
            return Result.failure(IllegalArgumentException("Draft has ${validation.errors.size} error(s)"))
        }

        val coordinate = SpaceDraftValidator.coordinate(draft)
            ?: return Result.failure(IllegalArgumentException("Missing coordinate"))

        val municipality = draft.municipality
            ?: return Result.failure(IllegalArgumentException("Missing municipality"))

        val localRecord = CulturalSpace(
            id = SpaceId.nextLocal(_spaces.value.map { it.id }),
            name = draft.name.trim(),
            address = draft.address.trim(),
            phone = mx.vendaval.core.domain.model.PhoneNumber.orNull(draft.phone),
            type = draft.type ?: VenueType.OTRO,
            municipality = municipality,
            location = coordinate,
            mapQuery = SpaceDraftValidator.deriveMapQuery(draft),
            origin = SpaceOrigin.LOCAL,
        )

        // Durable locally first: the user's contribution must survive a crash or
        // a dead network, and the UI can close the form immediately.
        applySnapshot(
            snapshot.copy(local = snapshot.local + localRecord.toDto()),
            persist = true,
        )

        publish(localRecord)
        return Result.success(localRecord)
    }

    override suspend fun retryPendingSubmissions() {
        val pending = snapshot.local.toList()
        if (pending.isEmpty()) return

        for (dto in pending) {
            val record = dto.toDomainOrNull(SpaceOrigin.LOCAL) ?: continue
            publish(record)
        }
    }

    override suspend fun deleteLocal(id: SpaceId): Boolean {
        if (!id.isLocal) return false
        val remaining = snapshot.local.filterNot { it.id == id.value }
        if (remaining.size == snapshot.local.size) return false
        applySnapshot(snapshot.copy(local = remaining), persist = true)
        return true
    }

    override fun realtimeInserts(): Flow<CulturalSpace> =
        realtimeSource.inserts()
            .map { dto -> dto.toDomainOrNull(SpaceOrigin.REMOTE) }
            .filterNotNull()

    /**
     * Attempts publication of one local record. On success the record moves from
     * the local list to the remote mirror under its server-assigned id, so it
     * stops being retried and stops showing the "pending" badge.
     */
    private suspend fun publish(record: CulturalSpace) {
        when (val result = api.insert(record.toInsertDto())) {
            is ApiResult.Success -> {
                applySnapshot(
                    snapshot.copy(
                        local = snapshot.local.filterNot { it.id == record.id.value },
                        remote = snapshot.remote.upsert(result.value),
                    ),
                    persist = true,
                )
            }

            is ApiResult.Failure -> {
                // Stays in the outbox. REJECTED is not retried on a loop because
                // an RLS denial will not fix itself, but it is kept locally so the
                // contribution is not lost.
                if (result.cause == SyncFailure.REJECTED) {
                    _syncState.value = SyncState.Failed(SyncFailure.REJECTED, clock.now())
                }
            }
        }
    }

    /**
     * Recomputes the merged catalogue and, optionally, persists the snapshot.
     * Single funnel for every mutation so state and disk cannot drift apart.
     */
    private suspend fun applySnapshot(next: CatalogueSnapshot, persist: Boolean) {
        val usable = if (next.schemaVersion != CatalogueSnapshot.CURRENT_SCHEMA) {
            // Format changed under us: drop the mirror and let the next refresh
            // repopulate it. Local contributions are preserved.
            CatalogueSnapshot(local = next.local)
        } else {
            next
        }

        snapshot = usable
        _spaces.value = merge(usable)
        if (persist) cache.write(usable)
    }

    private fun merge(snapshot: CatalogueSnapshot): List<CulturalSpace> {
        val merged = LinkedHashMap<Long, CulturalSpace>(64)

        SeedCatalogue.spaces.forEach { merged[it.id.value] = it }
        snapshot.remote.forEach { dto ->
            dto.toDomainOrNull(SpaceOrigin.REMOTE)?.let { merged[it.id.value] = it }
        }
        snapshot.local.forEach { dto ->
            dto.toDomainOrNull(SpaceOrigin.LOCAL)?.let { merged[it.id.value] = it }
        }

        return merged.values.toList()
    }
}

/** Replaces a row with the same id, or appends it. */
private fun List<SpaceDto>.upsert(dto: SpaceDto): List<SpaceDto> {
    val index = indexOfFirst { it.id != null && it.id == dto.id }
    return if (index >= 0) toMutableList().also { it[index] = dto } else this + dto
}
