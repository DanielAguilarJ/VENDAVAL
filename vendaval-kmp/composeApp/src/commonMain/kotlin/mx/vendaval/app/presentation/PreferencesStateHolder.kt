package mx.vendaval.app.presentation

import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mx.vendaval.core.data.repository.AppLanguage
import mx.vendaval.core.data.repository.PreferencesRepository
import mx.vendaval.core.data.repository.ThemeMode
import mx.vendaval.core.data.repository.UserPreferences

/**
 * App-scoped preferences.
 *
 * Thin by design: it exists to bridge suspending repository writes to synchronous UI
 * callbacks and to load the persisted values once at startup. All the state lives in
 * the repository, so there is no second copy to keep in sync.
 */
class PreferencesStateHolder(
    private val repository: PreferencesRepository,
) : StateHolder() {

    val preferences: StateFlow<UserPreferences> = repository.preferences

    init {
        scope.launch { repository.warmUp() }
    }

    fun setThemeMode(mode: ThemeMode) {
        scope.launch { repository.setThemeMode(mode) }
    }

    fun setLanguage(language: AppLanguage) {
        scope.launch { repository.setLanguage(language) }
    }
}
