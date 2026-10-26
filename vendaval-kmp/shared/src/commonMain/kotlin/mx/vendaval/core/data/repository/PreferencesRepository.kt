package mx.vendaval.core.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import mx.vendaval.core.data.local.PreferencesCache
import mx.vendaval.core.data.local.PreferencesSnapshot
import mx.vendaval.core.domain.model.SortMode

/** How the app resolves light vs dark. */
enum class ThemeMode { SYSTEM, DARK, LIGHT }

/** Supported interface languages. */
enum class AppLanguage(val tag: String) { ES("es"), EN("en") }

/** Everything the settings screen can change, as one immutable value. */
data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.DARK,
    val language: AppLanguage = AppLanguage.ES,
    val sortMode: SortMode = SortMode.default,
    val hasSeenOnboarding: Boolean = false,
)

/**
 * Persisted user preferences.
 *
 * Enum values are stored by name and parsed defensively, so removing or renaming
 * a member degrades to the default instead of throwing on a file written by an
 * older build.
 *
 * The default theme is dark rather than system-following: the design system is
 * built from the original portal's obsidian palette, and its light variant is a
 * secondary derivation.
 */
class PreferencesRepository(
    private val cache: PreferencesCache,
) {
    private val _preferences = MutableStateFlow(UserPreferences())
    val preferences: StateFlow<UserPreferences> = _preferences.asStateFlow()

    suspend fun warmUp() {
        _preferences.value = cache.read().toDomain()
    }

    suspend fun setThemeMode(mode: ThemeMode) = mutate { it.copy(themeMode = mode) }

    suspend fun setLanguage(language: AppLanguage) = mutate { it.copy(language = language) }

    suspend fun setSortMode(mode: SortMode) = mutate { it.copy(sortMode = mode) }

    suspend fun markOnboardingSeen() = mutate { it.copy(hasSeenOnboarding = true) }

    private suspend fun mutate(transform: (UserPreferences) -> UserPreferences) {
        val next = transform(_preferences.value)
        _preferences.value = next
        cache.write(next.toSnapshot())
    }
}

private fun PreferencesSnapshot.toDomain(): UserPreferences = UserPreferences(
    themeMode = ThemeMode.entries.firstOrNull { it.name == themeMode } ?: ThemeMode.DARK,
    language = AppLanguage.entries.firstOrNull { it.name == language } ?: AppLanguage.ES,
    sortMode = SortMode.entries.firstOrNull { it.name == sortMode } ?: SortMode.default,
    hasSeenOnboarding = hasSeenOnboarding,
)

private fun UserPreferences.toSnapshot(): PreferencesSnapshot = PreferencesSnapshot(
    themeMode = themeMode.name,
    language = language.name,
    sortMode = sortMode.name,
    hasSeenOnboarding = hasSeenOnboarding,
)
