package mx.vendaval.app.ui.map

import mx.vendaval.core.domain.model.GeoPoint
import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.sinh
import kotlin.math.tan

/**
 * Web Mercator tile arithmetic — the "slippy map" scheme every raster tile server
 * uses.
 *
 * Implemented here rather than pulled from a library because no map SDK covers all
 * three targets: Google Maps is Android-only, MapKit is Apple-only, and desktop has
 * neither. Twenty lines of projection maths, a tile fetcher and a Compose `Canvas`
 * give one map implementation that behaves identically everywhere — which is the
 * whole argument for building this app on Compose Multiplatform.
 */
object TileMath {

    /** Edge length of a raster tile in pixels, fixed by the tile scheme. */
    const val TILE_SIZE = 256

    const val MIN_ZOOM = 3f

    /** OSM raster tiles stop at 19; asking for more returns empty tiles. */
    const val MAX_ZOOM = 18f

    /** Number of tiles along one axis at [zoom]. */
    fun tileCount(zoom: Int): Int = 1 shl zoom

    fun longitudeToTileX(longitude: Double, zoom: Int): Double =
        (longitude + 180.0) / 360.0 * tileCount(zoom)

    /**
     * Latitude is non-linear in Mercator, hence the log-tangent term. Values are
     * clamped to the projection's limits (±85.0511°) because the transform
     * diverges at the poles.
     *
     * The result is clamped again on the way out: at exactly the limit the floating
     * point evaluation overshoots the world square by ~1e-10, which is harmless for
     * drawing but breaks the invariant every caller relies on when it turns this
     * value into a tile row index.
     */
    fun latitudeToTileY(latitude: Double, zoom: Int): Double {
        val clamped = latitude.coerceIn(-MERCATOR_LIMIT, MERCATOR_LIMIT)
        val radians = clamped * DEG_TO_RAD
        val count = tileCount(zoom).toDouble()
        val y = (1.0 - ln(tan(radians) + 1.0 / cos(radians)) / PI) / 2.0 * count
        return y.coerceIn(0.0, count)
    }

    fun tileXToLongitude(tileX: Double, zoom: Int): Double =
        tileX / tileCount(zoom) * 360.0 - 180.0

    fun tileYToLatitude(tileY: Double, zoom: Int): Double {
        val n = PI - 2.0 * PI * tileY / tileCount(zoom)
        return atan(sinh(n)) / DEG_TO_RAD
    }

    fun toTilePoint(point: GeoPoint, zoom: Int): TilePoint = TilePoint(
        x = longitudeToTileX(point.longitude, zoom),
        y = latitudeToTileY(point.latitude, zoom),
    )

    fun toGeoPoint(tilePoint: TilePoint, zoom: Int): GeoPoint = GeoPoint(
        latitude = tileYToLatitude(tilePoint.y, zoom),
        longitude = tileXToLongitude(tilePoint.x, zoom),
    )

    /**
     * Integer zoom whose tiles cover [spanDegrees] of longitude within
     * [viewportPx]. Used to frame a result set.
     */
    fun zoomToFit(spanDegrees: Double, viewportPx: Float): Float {
        if (spanDegrees <= 0.0) return 14f
        var zoom = MAX_ZOOM
        while (zoom > MIN_ZOOM) {
            val pixelsPerDegree = tileCount(zoom.toInt()) * TILE_SIZE / 360.0
            if (spanDegrees * pixelsPerDegree <= viewportPx) break
            zoom -= 1f
        }
        return zoom.coerceIn(MIN_ZOOM, MAX_ZOOM)
    }

    private const val MERCATOR_LIMIT = 85.05112878
    private const val DEG_TO_RAD = PI / 180.0
}

/** A position in fractional tile units at a given zoom. */
data class TilePoint(val x: Double, val y: Double)

/** Identity of one raster tile. */
data class TileKey(val zoom: Int, val x: Int, val y: Int)
