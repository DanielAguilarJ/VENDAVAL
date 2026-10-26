package mx.vendaval.core.domain.model

import kotlinx.datetime.Instant

/**
 * Why a sync attempt failed.
 *
 * A typed cause rather than a message string: the UI owns the wording (and its
 * translation), and the repository never has to know what language the user
 * reads. It also keeps server details out of the presentation layer.
 */
enum class SyncFailure {
    /** No usable network, or DNS/TLS never completed. */
    OFFLINE,

    /** The anon key was rejected, or row-level security denied the read. */
    REJECTED,

    /** Reached the server but it answered 5xx. */
    SERVER,

    /** Response arrived but did not match the expected schema. */
    MALFORMED,

    UNKNOWN,
}

/** Lifecycle of the remote catalogue mirror. */
sealed interface SyncState {

    /** Nothing attempted yet in this session. */
    data object Idle : SyncState

    data object Syncing : SyncState

    /**
     * Last attempt succeeded. [remoteCount] is how many rows the server had, which
     * is surfaced so the user can tell "up to date" from "server is empty".
     */
    data class Synced(val at: Instant, val remoteCount: Int) : SyncState

    /**
     * Last attempt failed. The cached catalogue is still being shown — this
     * state annotates the data rather than replacing it, which is the whole
     * point of the offline-first design.
     */
    data class Failed(val cause: SyncFailure, val at: Instant) : SyncState

    val isBusy: Boolean get() = this is Syncing
}
