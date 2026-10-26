package mx.vendaval.core.domain

import mx.vendaval.core.domain.geo.distanceKm
import mx.vendaval.core.domain.geo.formatDistance
import mx.vendaval.core.domain.model.GeoBounds
import mx.vendaval.core.domain.model.GeoPoint
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DistanceTest {

    @Test
    fun distance_between_a_point_and_itself_is_zero() {
        val point = GeoPoint(21.8804, -102.2965)
        assertEquals(0.0, distanceKm(point, point), absoluteTolerance = 1e-9)
    }

    @Test
    fun distance_matches_known_separation_of_two_venues() {
        // Instituto Cultural de Aguascalientes to Teatro Aguascalientes: ~2.3 km,
        // measured from the seeded coordinates.
        val instituto = GeoPoint(21.8804, -102.2965)
        val teatro = GeoPoint(21.8601, -102.2905)

        val km = distanceKm(instituto, teatro)

        assertTrue(
            abs(km - 2.33) < 0.15,
            "expected roughly 2.33 km between the two venues but got $km",
        )
    }

    @Test
    fun distance_is_symmetric() {
        val a = GeoPoint(22.2289, -102.3241)
        val b = GeoPoint(21.8465, -102.7188)

        assertEquals(distanceKm(a, b), distanceKm(b, a), absoluteTolerance = 1e-9)
    }

    @Test
    fun distance_across_the_state_is_plausible() {
        // Cosío in the north to Calvillo in the south-west: the widest pair in the
        // dataset. Anything outside 50-100 km means the projection is wrong.
        val cosio = GeoPoint(22.3661, -102.3005)
        val calvillo = GeoPoint(21.8465, -102.7188)

        val km = distanceKm(cosio, calvillo)

        assertTrue(km in 50.0..100.0, "state-wide distance out of range: $km")
    }

    @Test
    fun sub_kilometre_distances_are_formatted_in_metres() {
        assertEquals("300 m", formatDistance(0.3))
        assertEquals("1 m", formatDistance(0.0))
    }

    @Test
    fun short_distances_keep_one_decimal_and_long_ones_are_whole() {
        assertEquals("2.3 km", formatDistance(2.34))
        assertEquals("42 km", formatDistance(42.7))
    }
}

class GeoPointTest {

    @Test
    fun out_of_range_coordinates_are_rejected_at_the_boundary() {
        assertNull(GeoPoint.orNull(91.0, 0.0))
        assertNull(GeoPoint.orNull(0.0, 181.0))
        assertNull(GeoPoint.orNull(null, -102.0))
        assertNotNull(GeoPoint.orNull(21.88, -102.29))
    }

    @Test
    fun string_parsing_tolerates_surrounding_whitespace() {
        val point = GeoPoint.orNull(" 21.8804 ", " -102.2965 ")
        assertNotNull(point)
        assertEquals(21.8804, point.latitude)
    }

    @Test
    fun non_numeric_strings_yield_null_rather_than_throwing() {
        assertNull(GeoPoint.orNull("north", "west"))
        assertNull(GeoPoint.orNull("-102.", ""))
    }

    @Test
    fun bounds_contain_every_point_they_were_built_from() {
        val points = listOf(
            GeoPoint(21.8804, -102.2965),
            GeoPoint(22.2289, -102.3241),
            GeoPoint(21.8465, -102.7188),
        )

        val bounds = GeoBounds.around(points)
        assertNotNull(bounds)
        points.forEach { assertTrue(it in bounds, "$it fell outside $bounds") }
    }

    @Test
    fun bounds_of_an_empty_set_are_null_rather_than_degenerate() {
        assertNull(GeoBounds.around(emptyList()))
    }

    @Test
    fun bounds_exclude_a_point_outside_them() {
        val bounds = GeoBounds.around(listOf(GeoPoint(21.88, -102.29)), paddingDegrees = 0.01)
        assertNotNull(bounds)
        assertFalse(GeoPoint(30.0, -102.29) in bounds)
    }
}
