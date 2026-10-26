package mx.vendaval.core.domain.geo

import mx.vendaval.core.domain.model.GeoPoint
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/** Mean Earth radius (IUGG), in kilometres. */
private const val EARTH_RADIUS_KM = 6371.0088

private const val DEG_TO_RAD = 0.017453292519943295

/**
 * Great-circle distance in kilometres.
 *
 * Uses the haversine formula in its numerically stable `asin(sqrt(h))` form.
 * The law of cosines is cheaper but loses precision for the short distances
 * that dominate here — venues in the historic centre sit a few hundred metres
 * apart, and the "near me" ordering must not shuffle on rounding noise.
 */
fun distanceKm(from: GeoPoint, to: GeoPoint): Double {
    val dLat = (to.latitude - from.latitude) * DEG_TO_RAD
    val dLon = (to.longitude - from.longitude) * DEG_TO_RAD
    val lat1 = from.latitude * DEG_TO_RAD
    val lat2 = to.latitude * DEG_TO_RAD

    val h = sin(dLat / 2).pow(2) + cos(lat1) * cos(lat2) * sin(dLon / 2).pow(2)
    return 2 * EARTH_RADIUS_KM * asin(min(1.0, sqrt(h)))
}

/**
 * Human-readable distance, switching to metres below one kilometre because
 * "0.3 km" reads worse than "300 m" for walking distances in a city centre.
 */
fun formatDistance(km: Double): String = when {
    km < 0.95 -> "${(km * 1000).toInt().coerceAtLeast(1)} m"
    km < 10.0 -> "${roundTo1Decimal(km)} km"
    else -> "${km.toInt()} km"
}

private fun roundTo1Decimal(value: Double): String {
    val scaled = (value * 10).toLong()
    val whole = scaled / 10
    val fraction = scaled % 10
    return "$whole.$fraction"
}
