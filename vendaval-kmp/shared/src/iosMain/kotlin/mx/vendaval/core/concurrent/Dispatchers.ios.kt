package mx.vendaval.core.concurrent

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Kotlin/Native has no dedicated IO pool. `Default` is backed by a worker pool
 * sized to the core count, which is appropriate here: the app's disk work is a
 * handful of small JSON files, and network IO is already non-blocking inside the
 * Darwin engine.
 */
actual val ioDispatcher: CoroutineDispatcher = Dispatchers.Default
