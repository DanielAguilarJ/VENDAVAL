package mx.vendaval.core.data.local

import kotlinx.serialization.Serializable
import mx.vendaval.core.data.dto.SpaceDto

/**
 * On-disk shape of the catalogue cache.
 *
 * Remote and locally created rows are kept in separate lists so a refresh can
 * replace the server mirror wholesale without ever risking the user's own
 * unpublished contributions.
 *
 * [schemaVersion] is checked on read: a bump discards the cache and re-fetches
 * rather than attempting a migration, which is the right trade for a cache whose
 * authoritative copy lives on the server.
 */
@Serializable
data class CatalogueSnapshot(
    val schemaVersion: Int = CURRENT_SCHEMA,
    val remote: List<SpaceDto> = emptyList(),
    val local: List<SpaceDto> = emptyList(),
    val lastSyncEpochMillis: Long? = null,
) {
    companion object {
        const val CURRENT_SCHEMA = 1
        val EMPTY = CatalogueSnapshot()
    }
}

@Serializable
data class FavouritesSnapshot(
    val schemaVersion: Int = 1,
    val ids: Set<Long> = emptySet(),
) {
    companion object {
        val EMPTY = FavouritesSnapshot()
    }
}

/**
 * User preferences that must survive a restart. Deliberately tiny and additive
 * so new fields default cleanly on an old file.
 */
@Serializable
data class PreferencesSnapshot(
    val schemaVersion: Int = 1,
    val themeMode: String = "SYSTEM",
    val language: String = "ES",
    val sortMode: String = "NAME",
    val hasSeenOnboarding: Boolean = false,
) {
    companion object {
        val DEFAULT = PreferencesSnapshot()
    }
}
