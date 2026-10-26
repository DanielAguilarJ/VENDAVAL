package mx.vendaval.core.data.local

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import mx.vendaval.core.concurrent.ioDispatcher
import okio.FileSystem
import okio.IOException
import okio.Path

/**
 * Where the app may write. Supplied per platform because Android resolves it
 * from a `Context` and desktop from the user's home directory.
 */
interface StoragePaths {
    /** Directory that already exists (or can be created) and is app-private. */
    val root: Path
    val fileSystem: FileSystem
}

/**
 * A single JSON document on disk, guarded and written atomically.
 *
 * Chosen over an embedded SQL database deliberately. The catalogue is a bounded
 * collection of a few hundred records that the app always loads in full to filter
 * and to plot on a map, so a query engine would buy nothing while adding a
 * codegen step and three platform-specific native drivers. Everything below the
 * [read]/[write] pair is an implementation detail of this class, so swapping in
 * SQLDelight later touches no caller.
 *
 * Writes go to a sibling temp file and are then moved into place, so a crash or a
 * process kill mid-write can never leave a half-serialised document behind — the
 * previous snapshot survives intact.
 */
class JsonFileStore<T>(
    private val paths: StoragePaths,
    private val fileName: String,
    private val serializer: KSerializer<T>,
    private val json: Json,
    private val defaultValue: () -> T,
) {
    private val mutex = Mutex()

    private val file: Path get() = paths.root / fileName
    private val tempFile: Path get() = paths.root / "$fileName.tmp"

    /**
     * Reads the document, returning the default on absence *or* corruption.
     *
     * A cache that cannot be parsed is treated as a cache miss rather than an
     * error: the seeded catalogue is always available underneath, so refusing to
     * start over a stale schema would be a self-inflicted outage.
     */
    suspend fun read(): T = withContext(ioDispatcher) {
        mutex.withLock {
            runCatching {
                val fs = paths.fileSystem
                if (!fs.exists(file)) return@runCatching defaultValue()
                val raw = fs.read(file) { readUtf8() }
                if (raw.isBlank()) defaultValue() else json.decodeFromString(serializer, raw)
            }.getOrElse { defaultValue() }
        }
    }

    /** Serialises and replaces the document atomically. Returns false on IO failure. */
    suspend fun write(value: T): Boolean = withContext(ioDispatcher) {
        mutex.withLock {
            runCatching {
                val fs = paths.fileSystem
                fs.createDirectories(paths.root)
                fs.write(tempFile) { writeUtf8(json.encodeToString(serializer, value)) }
                fs.atomicMove(tempFile, file)
                true
            }.getOrElse { error ->
                if (error is IOException) {
                    runCatching { paths.fileSystem.delete(tempFile) }
                    false
                } else {
                    throw error
                }
            }
        }
    }

    /** Read-modify-write under the same lock, so concurrent callers cannot interleave. */
    suspend fun update(transform: (T) -> T): T = withContext(ioDispatcher) {
        mutex.withLock {
            val fs = paths.fileSystem
            val current = runCatching {
                if (!fs.exists(file)) defaultValue()
                else fs.read(file) { readUtf8() }
                    .takeIf { it.isNotBlank() }
                    ?.let { json.decodeFromString(serializer, it) }
                    ?: defaultValue()
            }.getOrElse { defaultValue() }

            val updated = transform(current)
            runCatching {
                fs.createDirectories(paths.root)
                fs.write(tempFile) { writeUtf8(json.encodeToString(serializer, updated)) }
                fs.atomicMove(tempFile, file)
            }
            updated
        }
    }
}

/** Shared JSON configuration: tolerant on read, compact on write. */
val VendavalJson: Json = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
    encodeDefaults = true
    isLenient = true
}
