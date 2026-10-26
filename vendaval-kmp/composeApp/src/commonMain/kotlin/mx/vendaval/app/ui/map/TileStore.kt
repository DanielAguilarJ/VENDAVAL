package mx.vendaval.app.ui.map

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.graphics.ImageBitmap
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsBytes
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext

/**
 * Raster tile source.
 *
 * OpenStreetMap's public tile servers are used, which their usage policy allows for
 * low-volume applications provided requests carry an identifying user agent (set on
 * the shared tile client) and are cached. A production deployment at scale should
 * point [urlTemplate] at its own tile server or a commercial provider — the policy
 * is not a suggestion and heavy clients do get blocked.
 */
data class TileSource(
    val urlTemplate: String = "https://tile.openstreetmap.org/{z}/{x}/{y}.png",
    val attribution: String = "© OpenStreetMap contributors",
) {
    fun urlFor(key: TileKey): String = urlTemplate
        .replace("{z}", key.zoom.toString())
        .replace("{x}", key.x.toString())
        .replace("{y}", key.y.toString())
}

/**
 * In-memory tile cache with concurrency limiting.
 *
 * Backed by a Compose snapshot map so a tile arriving from the network invalidates
 * exactly the frame that needs it, with no manual notification.
 *
 * Two limits matter and both are deliberate:
 *  - [maxTiles] caps memory. A 256×256 ARGB tile is 256 KB, so 120 tiles is ~30 MB;
 *    an unbounded cache on a long panning session would grow until the app is killed.
 *  - [MAX_CONCURRENT_FETCHES] caps parallel requests. A single zoom-out can reveal
 *    dozens of new tiles at once, and firing them all would both saturate the
 *    connection pool and look like abuse to the tile server.
 */
class TileStore(
    private val client: HttpClient,
    private val scope: CoroutineScope,
    private val source: TileSource = TileSource(),
    private val maxTiles: Int = DEFAULT_MAX_TILES,
) {
    private val tiles = mutableStateMapOf<TileKey, ImageBitmap>()

    /** Insertion order, used for eviction. Also tracks in-flight keys. */
    private val insertionOrder = ArrayDeque<TileKey>()
    private val inFlight = mutableSetOf<TileKey>()
    private val failed = mutableSetOf<TileKey>()

    private val fetchLimit = Semaphore(MAX_CONCURRENT_FETCHES)

    /**
     * Returns the tile if resident, otherwise starts a fetch and returns `null`.
     * Callers draw a placeholder for `null` and are recomposed when it lands.
     */
    fun tileOrLoad(key: TileKey): ImageBitmap? {
        tiles[key]?.let { return it }
        if (key !in inFlight && key !in failed) load(key)
        return null
    }

    private fun load(key: TileKey) {
        inFlight += key
        scope.launch {
            try {
                val bitmap = fetchLimit.withPermit {
                    val bytes = client.get(source.urlFor(key)).bodyAsBytes()
                    // Decoding is CPU-bound; keep it off the frame thread.
                    withContext(Dispatchers.Default) { decodeImageBytes(bytes) }
                }
                if (bitmap != null) put(key, bitmap) else failed += key
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Throwable) {
                // Remember the failure so a permanently missing tile is not
                // re-requested on every single frame.
                failed += key
            } finally {
                inFlight -= key
            }
        }
    }

    private fun put(key: TileKey, bitmap: ImageBitmap) {
        tiles[key] = bitmap
        insertionOrder.addLast(key)
        while (insertionOrder.size > maxTiles) {
            val oldest = insertionOrder.removeFirst()
            tiles.remove(oldest)
        }
    }

    /** Lets the user retry after connectivity returns. */
    fun clearFailures() {
        failed.clear()
    }

    val attribution: String get() = source.attribution

    private companion object {
        const val DEFAULT_MAX_TILES = 120
        const val MAX_CONCURRENT_FETCHES = 6
    }
}
