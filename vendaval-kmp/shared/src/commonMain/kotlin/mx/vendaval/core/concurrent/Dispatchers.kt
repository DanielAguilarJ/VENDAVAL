package mx.vendaval.core.concurrent

import kotlinx.coroutines.CoroutineDispatcher

/**
 * Dispatcher for blocking filesystem and socket work.
 *
 * `Dispatchers.IO` is a JVM concept, so it is declared here and supplied per
 * platform. Injecting it through this single symbol also means tests can swap in
 * a deterministic dispatcher without touching call sites.
 */
expect val ioDispatcher: CoroutineDispatcher
