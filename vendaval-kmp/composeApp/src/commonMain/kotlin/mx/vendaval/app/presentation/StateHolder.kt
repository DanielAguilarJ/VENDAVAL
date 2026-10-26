package mx.vendaval.app.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

/**
 * Minimal, multiplatform stand-in for `ViewModel`.
 *
 * `androidx.lifecycle`'s multiplatform artifacts were still moving at the time of
 * writing, and the only thing the screens here need from a ViewModel is a scope
 * that is cancelled when the owner leaves composition. That is small enough to own
 * outright, which removes a pre-release dependency from the critical path and keeps
 * the behaviour identical on Android, iOS and desktop.
 *
 * The scope uses [SupervisorJob] so one failed load — a refresh, say — does not
 * cancel unrelated collectors in the same holder.
 */
abstract class StateHolder {

    protected val scope: CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Main)

    /** Cancels all work. Idempotent; safe to call from `onDispose`. */
    open fun dispose() {
        scope.cancel()
    }
}

/**
 * Creates a holder tied to the composition that calls this.
 *
 * Use for screen-scoped state. App-scoped holders (the catalogue, preferences)
 * are constructed once at the root and passed down explicitly, so their lifetime
 * is obvious from the call graph instead of being implied by a store.
 */
@Composable
fun <T : StateHolder> rememberStateHolder(factory: () -> T): T {
    val holder = remember { factory() }
    DisposableEffect(holder) {
        onDispose { holder.dispose() }
    }
    return holder
}
