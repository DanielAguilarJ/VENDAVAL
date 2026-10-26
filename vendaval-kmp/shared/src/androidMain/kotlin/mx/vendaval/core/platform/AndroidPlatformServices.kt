package mx.vendaval.core.platform

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import androidx.core.content.ContextCompat
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

/** App-private internal storage; never on external media, never backed up to a share. */
class AndroidStoragePaths(context: Context) : StoragePaths {
    override val fileSystem: FileSystem = FileSystem.SYSTEM
    override val root: Path = "${context.filesDir.absolutePath}/vendaval".toPath()
}

/**
 * Location via the platform [LocationManager].
 *
 * Uses `getLastKnownLocation` across the available providers instead of
 * requesting a fresh fix, and deliberately avoids Play Services. The feature only
 * needs to order a list of venues by proximity — a fix that is a few minutes old
 * and a few hundred metres out changes nothing, while a live fix would cost a
 * dependency on Google Play and keep the GPS radio awake.
 */
class AndroidLocationProvider(private val context: Context) : LocationProvider {

    override val isSupported: Boolean = true

    override suspend fun currentLocation(): LocationOutcome {
        if (!hasAnyLocationPermission()) return LocationOutcome.PermissionDenied

        val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return LocationOutcome.Unavailable

        val best = PROVIDER_PREFERENCE
            .mapNotNull { provider -> lastKnown(manager, provider) }
            .maxByOrNull { it.time }
            ?: return LocationOutcome.Unavailable

        val point = GeoPoint.orNull(best.latitude, best.longitude)
            ?: return LocationOutcome.Unavailable

        return LocationOutcome.Available(point)
    }

    private fun lastKnown(manager: LocationManager, provider: String): Location? =
        try {
            if (manager.isProviderEnabled(provider)) manager.getLastKnownLocation(provider) else null
        } catch (_: SecurityException) {
            // Permission revoked between the check and the call.
            null
        } catch (_: IllegalArgumentException) {
            // Provider absent on this device.
            null
        }

    private fun hasAnyLocationPermission(): Boolean =
        listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            .any { permission ->
                ContextCompat.checkSelfPermission(context, permission) ==
                    PackageManager.PERMISSION_GRANTED
            }

    private companion object {
        val PROVIDER_PREFERENCE = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.PASSIVE_PROVIDER,
        )
    }
}

/**
 * Hands off to other installed apps.
 *
 * Maps use an implicit `geo:` intent first so the user's preferred map app wins,
 * falling back to a Google Maps web URL when nothing handles it — an emulator
 * without Play Services being the common case.
 */
class AndroidExternalLauncher(private val context: Context) : ExternalLauncher {

    override fun openMap(space: CulturalSpace) {
        val handled = start(Intent(Intent.ACTION_VIEW, Uri.parse(space.geoUri)))
        if (!handled) openUrl(space.googleMapsUrl)
    }

    override fun dial(phone: PhoneNumber) {
        // ACTION_DIAL, not ACTION_CALL: it needs no permission and leaves the
        // user in control of actually placing the call.
        start(Intent(Intent.ACTION_DIAL, Uri.parse(phone.dialUri)))
    }

    override fun openUrl(url: String) {
        start(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    override fun share(space: CulturalSpace) {
        val text = buildString {
            append(space.name)
            append(" · ")
            append(space.address)
            append("\n")
            append(space.googleMapsUrl)
        }
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, space.name)
            putExtra(Intent.EXTRA_TEXT, text)
        }
        start(Intent.createChooser(send, space.name))
    }

    /**
     * `NEW_TASK` is required because the launcher is constructed with the
     * application context, which has no task of its own.
     */
    private fun start(intent: Intent): Boolean = try {
        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        true
    } catch (_: ActivityNotFoundException) {
        false
    }
}
