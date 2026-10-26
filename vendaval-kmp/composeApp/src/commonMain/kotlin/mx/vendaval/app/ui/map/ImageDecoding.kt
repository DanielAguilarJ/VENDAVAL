package mx.vendaval.app.ui.map

import androidx.compose.ui.graphics.ImageBitmap

/**
 * Decodes an encoded PNG/JPEG byte array into a Compose bitmap.
 *
 * There is no common decoder: Android goes through `BitmapFactory`, while desktop
 * and iOS both go through Skia. Returns `null` on a malformed payload — a tile
 * server occasionally answers with an HTML error page, and that must show as a
 * missing tile rather than crash the map.
 */
expect fun decodeImageBytes(bytes: ByteArray): ImageBitmap?
