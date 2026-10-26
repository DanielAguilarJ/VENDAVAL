package mx.vendaval.core.platform

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mx.vendaval.core.data.local.StoragePaths
import mx.vendaval.core.domain.model.CulturalSpace
import mx.vendaval.core.domain.model.GeoPoint
import mx.vendaval.core.domain.model.PhoneNumber
import mx.vendaval.core.domain.service.ExternalLauncher
import mx.vendaval.core.domain.service.LocationOutcome
import mx.vendaval.core.domain.service.LocationProvider
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusDenied
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.CoreLocation.kCLAuthorizationStatusRestricted
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.Foundation.NSApplicationSupportDirectory
import platform.UIKit.UIApplication
import platform.UIKit.UIPasteboard

/**
 * Application Support inside the app sandbox: persisted, excluded from the
 * user-visible Files app, and included in device backups — appropriate for a
 * cache plus a handful of user favourites.
 */
class IosStoragePaths : StoragePaths {
    override val fileSystem: FileSystem = FileSystem.SYSTEM

    override val root: Path = run {
        val directories = NSSearchPathForDirectoriesInDomains(
            NSApplicationSupportDirectory,
            NSUserDomainMask,
            true,
        )
        val base = directories.firstOrNull() as? String ?: "."
        "$base/Vendaval".toPath()
    }
}

/**
 * Location through Core Location.
 *
 * Reads the cached `location` property rather than starting continuous updates:
 * ordering a venue list by proximity does not justify waking the GPS or holding a
 * delegate alive. When authorisation has not been requested yet the system prompt
 * is triggered and [LocationOutcome.PermissionDenied] is returned so the caller
 * re-asks after the user answers, instead of blocking on a delegate callback.
 *
 * Requires `NSLocationWhenInUseUsageDescription` in `Info.plist`; without it iOS
 * silently refuses the request.
 */
class IosLocationProvider : LocationProvider {

    override val isSupported: Boolean = true

    @OptIn(ExperimentalForeignApi::class)
    override suspend fun currentLocation(): LocationOutcome = withContext(Dispatchers.Main) {
        // CLLocationManager must be created and touched on the main thread.
        val manager = CLLocationManager()

        when (CLLocationManager.authorizationStatus()) {
            kCLAuthorizationStatusNotDetermined -> {
                manager.requestWhenInUseAuthorization()
                LocationOutcome.PermissionDenied
            }

            kCLAuthorizationStatusDenied, kCLAuthorizationStatusRestricted ->
                LocationOutcome.PermissionDenied

            kCLAuthorizationStatusAuthorizedWhenInUse, kCLAuthorizationStatusAuthorizedAlways -> {
                val location = manager.location
                if (location == null) {
                    LocationOutcome.Unavailable
                } else {
                    val point = location.coordinate.useContents {
                        GeoPoint.orNull(latitude, longitude)
                    }
                    point?.let(LocationOutcome::Available) ?: LocationOutcome.Unavailable
                }
            }

            else -> LocationOutcome.Unavailable
        }
    }
}

/** Opens Apple Maps, the dialler, Safari and the share sheet's text payload. */
class IosExternalLauncher : ExternalLauncher {

    override fun openMap(space: CulturalSpace) = openUrl(space.appleMapsUrl)

    override fun dial(phone: PhoneNumber) = openUrl(phone.dialUri)

    override fun openUrl(url: String) {
        val nsUrl = NSURL.URLWithString(url) ?: return
        val application = UIApplication.sharedApplication
        if (application.canOpenURL(nsUrl)) {
            application.openURL(nsUrl, options = emptyMap<Any?, Any>(), completionHandler = null)
        }
    }

    /**
     * Presenting `UIActivityViewController` needs a view controller, which this
     * layer intentionally has no reference to. Copying to the pasteboard keeps
     * the action useful without dragging UIKit presentation into a service class.
     */
    override fun share(space: CulturalSpace) {
        UIPasteboard.generalPasteboard.string =
            "${space.name} · ${space.address}\n${space.googleMapsUrl}"
    }
}
