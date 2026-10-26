package mx.vendaval.app.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import mx.vendaval.core.domain.model.GeoBounds
import mx.vendaval.core.domain.model.GeoPoint
import kotlin.math.pow

/**
 * The map viewport: where it is centred and how far in it is zoomed.
 *
 * Zoom is fractional so pinch gestures are continuous rather than snapping between
 * integer levels. Tiles are still fetched at the integer level below and drawn
 * scaled by the fractional remainder, which is exactly how native map renderers
 * handle intermediate zoom.
 */
class MapCameraState(
    initialCentre: GeoPoint = GeoPoint.AGUASCALIENTES_CENTRE,
    initialZoom: Float = 12f,
) {
    var centre: GeoPoint by mutableStateOf(initialCentre)
        private set

    private var zoomState = mutableFloatStateOf(
        initialZoom.coerceIn(TileMath.MIN_ZOOM, TileMath.MAX_ZOOM),
    )

    var zoom: Float
        get() = zoomState.value
        private set(value) {
            zoomState.value = value.coerceIn(TileMath.MIN_ZOOM, TileMath.MAX_ZOOM)
        }

    /** Integer zoom whose tiles are fetched. */
    val tileZoom: Int get() = zoom.toInt()

    /** Scale applied to those tiles, always in `[1, 2)`. */
    val tileScale: Float get() = 2f.pow(zoom - tileZoom)

    /** On-screen edge length of one tile at the current zoom. */
    val tilePixelSize: Float get() = TileMath.TILE_SIZE * tileScale

    fun moveTo(point: GeoPoint, zoom: Float = this.zoom) {
        centre = point
        this.zoom = zoom
    }

    /** Pans by a screen-space drag, converting pixels to tile units. */
    fun panBy(delta: Offset) {
        val tileSize = tilePixelSize
        if (tileSize <= 0f) return

        val current = TileMath.toTilePoint(centre, tileZoom)
        val moved = TilePoint(
            x = current.x - delta.x / tileSize,
            y = current.y - delta.y / tileSize,
        )

        val limit = TileMath.tileCount(tileZoom).toDouble()
        centre = TileMath.toGeoPoint(
            TilePoint(
                // Longitude wraps; latitude is clamped because there is no world
                // above the projection limit to scroll into.
                x = moved.x.mod(limit),
                y = moved.y.coerceIn(0.0, limit),
            ),
            tileZoom,
        )
    }

    fun zoomBy(delta: Float) {
        zoom += delta
    }

    /**
     * Pinch or scroll zoom anchored at a screen position, so the point under the
     * fingers stays put — without this the map appears to slide away while zooming.
     */
    fun zoomAround(delta: Float, focus: Offset, viewport: Size) {
        if (viewport.width <= 0f || viewport.height <= 0f) {
            zoomBy(delta)
            return
        }

        val focusGeo = screenToGeo(focus, viewport)
        zoom += delta
        val focusAfter = screenToGeo(focus, viewport)

        // Shift the centre by however much the anchor drifted.
        centre = GeoPoint(
            latitude = centre.latitude + (focusGeo.latitude - focusAfter.latitude),
            longitude = centre.longitude + (focusGeo.longitude - focusAfter.longitude),
        )
    }

    /** Frames [bounds] with the tightest zoom that still fits [viewport]. */
    fun fitTo(bounds: GeoBounds, viewport: Size) {
        val span = (bounds.east - bounds.west).coerceAtLeast(0.001)
        moveTo(bounds.centre, TileMath.zoomToFit(span, viewport.width))
    }

    fun geoToScreen(point: GeoPoint, viewport: Size): Offset {
        val centreTile = TileMath.toTilePoint(centre, tileZoom)
        val target = TileMath.toTilePoint(point, tileZoom)
        val tileSize = tilePixelSize
        return Offset(
            x = ((target.x - centreTile.x) * tileSize + viewport.width / 2f).toFloat(),
            y = ((target.y - centreTile.y) * tileSize + viewport.height / 2f).toFloat(),
        )
    }

    fun screenToGeo(position: Offset, viewport: Size): GeoPoint {
        val centreTile = TileMath.toTilePoint(centre, tileZoom)
        val tileSize = tilePixelSize
        return TileMath.toGeoPoint(
            TilePoint(
                x = centreTile.x + (position.x - viewport.width / 2f) / tileSize,
                y = centreTile.y + (position.y - viewport.height / 2f) / tileSize,
            ),
            tileZoom,
        )
    }

    companion object {
        /** Survives configuration changes and process death on Android. */
        val Saver: Saver<MapCameraState, List<Any>> = Saver(
            save = { listOf(it.centre.latitude, it.centre.longitude, it.zoom) },
            restore = { saved ->
                MapCameraState(
                    initialCentre = GeoPoint(saved[0] as Double, saved[1] as Double),
                    initialZoom = saved[2] as Float,
                )
            },
        )
    }
}

@Composable
fun rememberMapCameraState(
    initialCentre: GeoPoint = GeoPoint.AGUASCALIENTES_CENTRE,
    initialZoom: Float = 12f,
): MapCameraState = rememberSaveable(saver = MapCameraState.Saver) {
    MapCameraState(initialCentre, initialZoom)
}
