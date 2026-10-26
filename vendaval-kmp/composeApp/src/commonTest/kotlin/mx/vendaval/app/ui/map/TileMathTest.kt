package mx.vendaval.app.ui.map

import mx.vendaval.core.domain.model.GeoPoint
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Projection tests.
 *
 * The map is drawn by hand, so an error here would not throw — it would silently put
 * every marker in the wrong place. These pin the transform against values that can be
 * checked independently against any slippy-map tile calculator.
 */
class TileMathTest {

    @Test
    fun zoom_zero_is_a_single_tile() {
        assertEquals(1, TileMath.tileCount(0))
        assertEquals(4, TileMath.tileCount(2))
        assertEquals(4096, TileMath.tileCount(12))
    }

    @Test
    fun the_null_island_sits_at_the_centre_of_the_world() {
        assertEquals(0.5, TileMath.longitudeToTileX(0.0, 0), absoluteTolerance = 1e-9)
        assertEquals(0.5, TileMath.latitudeToTileY(0.0, 0), absoluteTolerance = 1e-9)
    }

    @Test
    fun the_antimeridian_maps_to_the_world_edges() {
        assertEquals(0.0, TileMath.longitudeToTileX(-180.0, 0), absoluteTolerance = 1e-9)
        assertEquals(1.0, TileMath.longitudeToTileX(180.0, 0), absoluteTolerance = 1e-9)
    }

    @Test
    fun aguascalientes_lands_on_the_expected_tile_at_zoom_twelve() {
        // Cross-checked against the reference slippy-map formula: lon -102.2916,
        // lat 21.8853 at z12 is tile x=884, y=1792.
        val point = GeoPoint(21.8853, -102.2916)

        val x = TileMath.longitudeToTileX(point.longitude, 12).toInt()
        val y = TileMath.latitudeToTileY(point.latitude, 12).toInt()

        assertEquals(884, x)
        assertEquals(1792, y)
    }

    @Test
    fun the_projection_round_trips_within_a_metre() {
        val original = GeoPoint(21.8804, -102.2965)

        val tile = TileMath.toTilePoint(original, 16)
        val restored = TileMath.toGeoPoint(tile, 16)

        assertTrue(
            abs(restored.latitude - original.latitude) < 1e-9,
            "latitude drifted: ${restored.latitude} vs ${original.latitude}",
        )
        assertTrue(
            abs(restored.longitude - original.longitude) < 1e-9,
            "longitude drifted: ${restored.longitude} vs ${original.longitude}",
        )
    }

    @Test
    fun latitude_is_clamped_to_the_mercator_limits_instead_of_diverging() {
        // Beyond ±85.0511° the transform tends to infinity; the clamp keeps the result
        // finite and inside the world square.
        val northPole = TileMath.latitudeToTileY(90.0, 4)
        val southPole = TileMath.latitudeToTileY(-90.0, 4)

        assertTrue(northPole.isFinite() && southPole.isFinite())
        assertTrue(northPole in 0.0..TileMath.tileCount(4).toDouble())
        assertTrue(southPole in 0.0..TileMath.tileCount(4).toDouble())
    }

    @Test
    fun latitude_increases_northwards_meaning_tile_y_decreases() {
        val south = TileMath.latitudeToTileY(21.80, 12)
        val north = TileMath.latitudeToTileY(22.30, 12)
        assertTrue(north < south, "tile Y must grow towards the south")
    }

    @Test
    fun zoom_to_fit_is_bounded_and_narrows_as_the_span_shrinks() {
        val wide = TileMath.zoomToFit(spanDegrees = 1.0, viewportPx = 1000f)
        val tight = TileMath.zoomToFit(spanDegrees = 0.01, viewportPx = 1000f)

        assertTrue(wide in TileMath.MIN_ZOOM..TileMath.MAX_ZOOM)
        assertTrue(tight in TileMath.MIN_ZOOM..TileMath.MAX_ZOOM)
        assertTrue(tight >= wide, "a smaller span should allow at least as much zoom")
    }

    @Test
    fun tile_urls_substitute_every_placeholder() {
        val url = TileSource().urlFor(TileKey(zoom = 12, x = 884, y = 1792))
        assertEquals("https://tile.openstreetmap.org/12/884/1792.png", url)
    }
}
