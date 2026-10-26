package mx.vendaval.core.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import mx.vendaval.core.data.local.FavouritesSnapshot
import mx.vendaval.core.data.local.FavouritesCache
import mx.vendaval.core.domain.model.SpaceId
import mx.vendaval.core.domain.repository.FavouritesRepository

/**
 * Device-local favourites.
 *
 * Held in memory as the read path and mirrored to disk on every mutation. The
 * write is awaited rather than fired and forgotten so a toggle followed
 * immediately by app death is still durable, but the in-memory flow updates
 * first so the star animates without waiting on IO.
 *
 * These ids never leave the device — there is no account system, and a public
 * anon key could not protect per-user data even if there were.
 */
class FavouritesRepositoryImpl(
    private val cache: FavouritesCache,
) : FavouritesRepository {

    private val _favourites = MutableStateFlow<Set<SpaceId>>(emptySet())
    override val favourites: StateFlow<Set<SpaceId>> = _favourites.asStateFlow()

    private var loaded = false

    suspend fun warmUp() {
        if (loaded) return
        loaded = true
        _favourites.value = cache.read().ids.map(::SpaceId).toSet()
    }

    override suspend fun toggle(id: SpaceId) {
        val current = _favourites.value
        val next = if (id in current) current - id else current + id
        _favourites.value = next
        cache.write(FavouritesSnapshot(ids = next.map { it.value }.toSet()))
    }

    override suspend fun isFavourite(id: SpaceId): Boolean = id in _favourites.value
}
