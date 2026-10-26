package mx.vendaval.core.domain.service

import mx.vendaval.core.domain.model.CulturalSpace
import mx.vendaval.core.domain.model.GeoPoint
import mx.vendaval.core.domain.model.PhoneNumber

/**
 * Result of asking the platform where the user is.
 *
 * Modelled explicitly instead of returning a nullable point so the UI can tell
 * "the user said no" (offer a manual reference point) from "we are still
 * waiting" (show a spinner) from "this device has no GPS" (hide the affordance
 * entirely, which is the desktop case).
 */
sealed interface LocationOutcome {
    data class Available(val point: GeoPoint) : LocationOutcome
    data object PermissionDenied : LocationOutcome
    data object Unavailable : LocationOutcome
    data object Unsupported : LocationOutcome
}

/**
 * Platform location access.
 *
 * Declared as an interface rather than `expect`/`actual` because the Android
 * implementation needs a `Context` injected — an interface lets DI supply it and
 * lets tests supply a fake with zero ceremony.
 */
interface LocationProvider {
    /** False on platforms with no location services, so the UI can hide "near me". */
    val isSupported: Boolean

    suspend fun currentLocation(): LocationOutcome
}

/**
 * Hands off to other apps: maps, dialler, browser.
 *
 * Each platform picks the most native affordance (an implicit `geo:` intent on
 * Android, Apple Maps on iOS, the default browser on desktop) while callers stay
 * platform-agnostic.
 */
interface ExternalLauncher {
    fun openMap(space: CulturalSpace)
    fun dial(phone: PhoneNumber)
    fun openUrl(url: String)
    fun share(space: CulturalSpace)
}
