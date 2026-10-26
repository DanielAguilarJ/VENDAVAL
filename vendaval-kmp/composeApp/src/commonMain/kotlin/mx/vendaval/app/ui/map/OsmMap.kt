package mx.vendaval.app.ui.map

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import mx.vendaval.app.theme.Tokens
import mx.vendaval.app.theme.markerColour
import mx.vendaval.core.domain.model.GeoPoint
import mx.vendaval.core.domain.model.SpaceId
import mx.vendaval.core.domain.model.SpaceListItem
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.hypot
import kotlin.math.ln
import kotlin.math.roundToInt

/**
 * Interactive raster map, drawn entirely with Compose primitives.
 *
 * One implementation serves Android, iOS and desktop. Tiles are painted straight
 * onto a `Canvas` and markers are drawn on top, so there is no platform view to
 * embed, no interop layer, and no per-platform map SDK to keep in sync.
 *
 * Tile lookups happen in the draw phase. That is intentional: [TileStore] is backed
 * by a snapshot map, so a tile arriving over the network invalidates only the draw
 * pass rather than recomposing the tree, which is the cheapest way to get a
 * progressively-filling map.
 */
@Composable
fun OsmMap(
    items: List<SpaceListItem>,
    camera: MapCameraState,
    tileStore: TileStore,
    selectedId: SpaceId?,
    onMarkerClick: (SpaceId) -> Unit,
    modifier: Modifier = Modifier,
    userLocation: GeoPoint? = null,
    overlay: @Composable BoxScope.() -> Unit = {},
) {
    // OSM's standard tiles are beige and bright. Against this app's near-black
    // chrome that seam read as unfinished, so in dark mode the tiles are inverted
    // and calmed rather than swapped for a third-party dark basemap: inverting keeps
    // OpenStreetMap as the only source, so the attribution stays truthful and no new
    // provider's usage terms come along with it. Label text survives because
    // inverting dark-on-light gives light-on-dark, which is what we want anyway.
    val isDarkMap = MaterialTheme.colorScheme.background == Tokens.Ink
    val tileFilter: ColorFilter? = remember(isDarkMap) {
        if (!isDarkMap) return@remember null
        val matrix = ColorMatrix(
            floatArrayOf(
                -1f, 0f, 0f, 0f, 255f,
                0f, -1f, 0f, 0f, 255f,
                0f, 0f, -1f, 0f, 255f,
                0f, 0f, 0f, 1f, 0f,
            ),
        )
        // Inversion alone turns greens magenta and water orange. Pulling saturation
        // most of the way out leaves a neutral graphite basemap instead of a
        // photographic negative.
        matrix.timesAssign(ColorMatrix().apply { setToSaturation(0.28f) })
        ColorFilter.colorMatrix(matrix)
    }

    val placeholder = if (MaterialTheme.colorScheme.background == Tokens.Ink) {
        Tokens.Card
    } else {
        Color(0xFFE7EDF4)
    }

    Box(modifier) {
        Canvas(
            modifier = Modifier
                .matchParentSize()
                // Declared before the transform gesture so unconsumed taps reach it.
                .pointerInput(items, camera.zoom, camera.centre) {
                    detectTapGestures { position ->
                        val viewport = Size(size.width.toFloat(), size.height.toFloat())
                        hitTest(items, camera, viewport, position)?.let(onMarkerClick)
                    }
                }
                .pointerInput(Unit) {
                    detectTransformGestures { centroid, pan, zoomChange, _ ->
                        val viewport = Size(size.width.toFloat(), size.height.toFloat())
                        if (pan != Offset.Zero) camera.panBy(pan)
                        if (zoomChange != 1f) {
                            // Zoom levels are powers of two, so a multiplicative
                            // pinch factor maps to an additive log2 delta.
                            camera.zoomAround(log2(zoomChange), centroid, viewport)
                        }
                    }
                }
                .pointerInput(Unit) {
                    // Desktop scroll wheel and trackpad. Harmless on touch, where
                    // scroll events are never produced.
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            if (event.type != PointerEventType.Scroll) continue
                            val change = event.changes.firstOrNull() ?: continue
                            val delta = change.scrollDelta.y
                            if (delta == 0f) continue
                            val viewport = Size(size.width.toFloat(), size.height.toFloat())
                            camera.zoomAround(-delta * SCROLL_ZOOM_STEP, change.position, viewport)
                            change.consume()
                        }
                    }
                },
        ) {
            drawTiles(camera, tileStore, placeholder, tileFilter)
            userLocation?.let { drawUserLocation(camera, it) }
            drawMarkers(items, camera, selectedId)
        }

        // OpenStreetMap's licence requires visible attribution.
        Text(
            text = tileStore.attribution,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.85f),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(6.dp)
                .background(Color.Black.copy(alpha = 0.45f), RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp),
        )

        overlay()
    }
}

/**
 * Paints the tile grid covering the viewport, plus a one-tile margin so a drag
 * never exposes an unpainted edge before the next frame.
 */
private fun DrawScope.drawTiles(
    camera: MapCameraState,
    tileStore: TileStore,
    placeholder: Color,
    tileFilter: ColorFilter?,
) {
    val zoom = camera.tileZoom
    val tileSize = camera.tilePixelSize
    if (tileSize <= 0f) return

    val centreTile = TileMath.toTilePoint(camera.centre, zoom)
    val tileCount = TileMath.tileCount(zoom)

    val halfCols = size.width / 2f / tileSize
    val halfRows = size.height / 2f / tileSize

    val firstX = floor(centreTile.x - halfCols).toInt()
    val lastX = ceil(centreTile.x + halfCols).toInt()
    val firstY = floor(centreTile.y - halfRows).toInt()
    val lastY = ceil(centreTile.y + halfRows).toInt()

    val drawSize = IntSize(ceil(tileSize).toInt(), ceil(tileSize).toInt())

    for (tileY in firstY..lastY) {
        // No world exists above or below the projection limits.
        if (tileY < 0 || tileY >= tileCount) continue

        for (tileX in firstX..lastX) {
            // Longitude wraps, so a tile index past the edge maps back around.
            val wrappedX = tileX.mod(tileCount)
            val left = ((tileX - centreTile.x) * tileSize + size.width / 2f).toFloat()
            val top = ((tileY - centreTile.y) * tileSize + size.height / 2f).toFloat()

            val bitmap = tileStore.tileOrLoad(TileKey(zoom, wrappedX, tileY))
            if (bitmap != null) {
                drawImage(
                    image = bitmap,
                    dstOffset = IntOffset(left.roundToInt(), top.roundToInt()),
                    dstSize = drawSize,
                    colorFilter = tileFilter,
                )
            } else {
                drawRect(
                    color = placeholder,
                    topLeft = Offset(left, top),
                    size = Size(tileSize, tileSize),
                )
            }
        }
    }
}

/**
 * Venue markers.
 *
 * Drawn back-to-front with the selected marker last so it is never occluded, and
 * culled to the viewport plus a margin: plotting a few hundred off-screen circles
 * per frame is pure waste once the catalogue grows.
 */
private fun DrawScope.drawMarkers(
    items: List<SpaceListItem>,
    camera: MapCameraState,
    selectedId: SpaceId?,
) {
    val viewport = size
    val margin = MARKER_RADIUS * 3f

    var selected: Pair<Offset, SpaceListItem>? = null

    for (item in items) {
        val position = camera.geoToScreen(item.space.location, viewport)
        if (position.x < -margin || position.x > viewport.width + margin) continue
        if (position.y < -margin || position.y > viewport.height + margin) continue

        if (item.id == selectedId) {
            selected = position to item
            continue
        }
        drawMarker(position, markerColour(item.space.type), isSelected = false)
    }

    selected?.let { (position, item) ->
        drawMarker(position, markerColour(item.space.type), isSelected = true)
    }
}

private fun DrawScope.drawMarker(position: Offset, colour: Color, isSelected: Boolean) {
    val radius = if (isSelected) MARKER_RADIUS * 1.45f else MARKER_RADIUS

    if (isSelected) {
        // Halo, which is what makes the active pin findable on a dense map.
        drawCircle(color = colour.copy(alpha = 0.28f), radius = radius * 2.4f, center = position)
    }

    // Dark base ring keeps light markers legible over pale tiles.
    drawCircle(color = Color(0xFF030712).copy(alpha = 0.55f), radius = radius + 3f, center = position)
    drawCircle(color = Color.White, radius = radius + 1.5f, center = position)
    drawCircle(color = colour, radius = radius, center = position)
}

private fun DrawScope.drawUserLocation(camera: MapCameraState, point: GeoPoint) {
    val position = camera.geoToScreen(point, size)
    drawCircle(color = Tokens.Accent.copy(alpha = 0.18f), radius = 26f, center = position)
    drawCircle(color = Color.White, radius = 8f, center = position)
    drawCircle(color = Tokens.Accent, radius = 5.5f, center = position)
}

/**
 * Finds the marker under a tap.
 *
 * The threshold is generous — larger than the drawn circle — because a fingertip is
 * far bigger than a 7 dp pin, and picking the *closest* candidate rather than the
 * first match means overlapping venues in the historic centre still resolve to the
 * one the user aimed at.
 */
private fun hitTest(
    items: List<SpaceListItem>,
    camera: MapCameraState,
    viewport: Size,
    tap: Offset,
): SpaceId? {
    var best: SpaceId? = null
    var bestDistance = TAP_THRESHOLD_PX

    for (item in items) {
        val position = camera.geoToScreen(item.space.location, viewport)
        val distance = hypot(position.x - tap.x, position.y - tap.y)
        if (distance <= bestDistance) {
            bestDistance = distance
            best = item.id
        }
    }
    return best
}

private fun log2(value: Float): Float = ln(value.toDouble()).toFloat() / LN_2

private const val LN_2 = 0.6931472f
private const val MARKER_RADIUS = 7f
private const val TAP_THRESHOLD_PX = 26f
private const val SCROLL_ZOOM_STEP = 0.35f
