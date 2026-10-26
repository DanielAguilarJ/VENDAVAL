package mx.vendaval.app.ui.map

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsBytes
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import mx.vendaval.core.data.remote.createTileHttpClient
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * End-to-end check of the tile pipeline on the JVM: coordinate to tile key, key to URL,
 * URL to bytes, bytes to a Compose bitmap.
 *
 * This is the one part of the map that unit tests cannot fully cover, because the
 * failure mode is a silently blank canvas rather than an exception. Living in `jvmTest`
 * keeps it out of the common suite, and it is written to skip rather than fail when the
 * machine has no network, so it never turns into a flaky gate.
 */
class TilePipelineTest {

    @Test
    fun a_real_tile_downloads_and_decodes_to_a_256_pixel_bitmap() = runTest {
        val client = createTileHttpClient()
        // The tile that actually covers central Aguascalientes at z12.
        val key = TileKey(zoom = 12, x = 884, y = 1792)
        val url = TileSource().urlFor(key)

        val bytes = try {
            withTimeout(15_000) { client.get(url).bodyAsBytes() }
        } catch (_: Throwable) {
            println("Skipping: tile server unreachable from this machine")
            client.close()
            return@runTest
        }

        assertTrue(bytes.size > 1_000, "a real tile should be more than a kilobyte")

        val bitmap = decodeImageBytes(bytes)
        assertNotNull(bitmap, "the tile bytes failed to decode")
        assertEquals(TileMath.TILE_SIZE, bitmap.width)
        assertEquals(TileMath.TILE_SIZE, bitmap.height)

        client.close()
    }

    @Test
    fun garbage_bytes_decode_to_null_instead_of_throwing() {
        // A tile server answering with an HTML error page must show as a missing tile,
        // not take the map down.
        assertNull(decodeImageBytes("<html>404</html>".encodeToByteArray()))
        assertNull(decodeImageBytes(ByteArray(0)))
    }
}
