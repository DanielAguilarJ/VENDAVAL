package mx.vendaval.app.ui.photo

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import vendaval_kmp.composeapp.generated.resources.Res
import mx.vendaval.app.ui.map.decodeImageBytes

/**
 * Decodes bundled venue photographs on demand and keeps a bounded number in memory.
 *
 * The shape follows [mx.vendaval.app.ui.map.TileStore], which has been carrying the
 * map for a while: a Compose snapshot map so an arrival invalidates only the row
 * that needs it, a permit so a fast scroll cannot start thirty decodes at once, and
 * a failure set so a bad asset is attempted once rather than on every
 * recomposition.
 *
 * It reads from resources, not the network. That was not the first design — an
 * HTTP version shipped, ran, and was measured, and 7 of 13 photographs silently
 * failed: Wikimedia answers a burst with HTTP 429, the shared client is configured
 * with `expectSuccess = false`, so the app decoded the error page, got null, and
 * rendered "this venue has no photograph". A wrong absence identical in appearance
 * to a correct one. Bundling removes the failure mode entirely and, more
 * importantly, makes photographs work offline — which for an app whose status line
 * reads "showing saved data" is the point rather than a bonus.
 *
 * Decoding is still worth deferring and bounding: `decodeImageBytes` has no
 * downsampling on any platform, so each 720 px image costs a couple of megabytes
 * of bitmap. The ingestor caps the stored size for exactly this reason; this class
 * must not be pointed at arbitrary images.
 */
class PhotoStore(
    private val scope: CoroutineScope,
    private val maxPhotos: Int = DEFAULT_MAX_PHOTOS,
) {
    private val photos = mutableStateMapOf<String, ImageBitmap>()
    private val insertionOrder = ArrayDeque<String>(maxPhotos)
    private val inFlight = mutableSetOf<String>()
    private val failed = mutableSetOf<String>()
    private val decodeLimit = Semaphore(MAX_CONCURRENT_DECODES)

    /**
     * The decoded photo for [assetPath], starting a decode if this is the first ask.
     *
     * Returns null while decoding, and null permanently if the asset could not be
     * read — callers must have something to show in both cases, which for venues is
     * the generated letter cover.
     */
    fun photoOrLoad(assetPath: String): ImageBitmap? {
        photos[assetPath]?.let { return it }
        if (assetPath in inFlight || assetPath in failed) return null
        inFlight += assetPath
        scope.launch { load(assetPath) }
        return null
    }

    private suspend fun load(assetPath: String) {
        try {
            val bitmap = decodeLimit.withPermit {
                val bytes = Res.readBytes(assetPath)
                withContext(Dispatchers.Default) { decodeImageBytes(bytes) }
            }
            if (bitmap == null) failed += assetPath else put(assetPath, bitmap)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Throwable) {
            // A missing asset means the generated seed and the resource directory
            // disagree, which VenuePhotoSeedTest is there to catch at build time.
            failed += assetPath
        } finally {
            inFlight -= assetPath
        }
    }

    private fun put(assetPath: String, bitmap: ImageBitmap) {
        photos[assetPath] = bitmap
        insertionOrder.addLast(assetPath)
        while (insertionOrder.size > maxPhotos) {
            photos.remove(insertionOrder.removeFirst())
        }
    }

    private companion object {
        const val DEFAULT_MAX_PHOTOS = 40
        const val MAX_CONCURRENT_DECODES = 3
    }
}
