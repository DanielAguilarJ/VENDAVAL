package mx.vendaval.core.domain.model

/**
 * A WGS-84 coordinate.
 *
 * Construction is deliberately permissive: coordinates arrive from a public,
 * user-writable table and from free-text form fields, so the type never throws.
 * Validity is an explicit query, and [orNull] is the boundary factory used when
 * decoding untrusted input.
 */
data class GeoPoint(
    val latitude: Double,
    val longitude: Double,
) {
    val isValid: Boolean
        get() = !latitude.isNaN() && !longitude.isNaN() &&
            latitude in -90.0..90.0 &&
            longitude in -180.0..180.0

    companion object {
        /** Geographic centre used to frame the map before any data loads. */
        val AGUASCALIENTES_CENTRE = GeoPoint(21.8853, -102.2916)

        fun orNull(latitude: Double?, longitude: Double?): GeoPoint? {
            if (latitude == null || longitude == null) return null
            return GeoPoint(latitude, longitude).takeIf { it.isValid }
        }

        fun orNull(latitude: String?, longitude: String?): GeoPoint? =
            orNull(latitude?.trim()?.toDoubleOrNull(), longitude?.trim()?.toDoubleOrNull())
    }
}

/**
 * An axis-aligned geographic rectangle, used to frame the map around a result
 * set and to cull markers that fall outside the viewport.
 */
data class GeoBounds(
    val south: Double,
    val west: Double,
    val north: Double,
    val east: Double,
) {
    val centre: GeoPoint get() = GeoPoint((south + north) / 2.0, (west + east) / 2.0)

    operator fun contains(point: GeoPoint): Boolean =
        point.latitude in south..north && point.longitude in west..east

    companion object {
        /**
         * Tight bounding box over [points], padded by [paddingDegrees] so
         * markers are not flush against the viewport edge. Returns `null` for an
         * empty input rather than a degenerate box.
         */
        fun around(points: Collection<GeoPoint>, paddingDegrees: Double = 0.01): GeoBounds? {
            if (points.isEmpty()) return null
            var south = Double.MAX_VALUE
            var west = Double.MAX_VALUE
            var north = -Double.MAX_VALUE
            var east = -Double.MAX_VALUE
            for (p in points) {
                if (p.latitude < south) south = p.latitude
                if (p.latitude > north) north = p.latitude
                if (p.longitude < west) west = p.longitude
                if (p.longitude > east) east = p.longitude
            }
            return GeoBounds(
                south = (south - paddingDegrees).coerceAtLeast(-90.0),
                west = (west - paddingDegrees).coerceAtLeast(-180.0),
                north = (north + paddingDegrees).coerceAtMost(90.0),
                east = (east + paddingDegrees).coerceAtMost(180.0),
            )
        }
    }
}
