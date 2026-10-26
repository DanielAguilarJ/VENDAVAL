package mx.vendaval.core.platform

import mx.vendaval.core.data.local.StoragePaths
import mx.vendaval.core.domain.model.CulturalSpace
import mx.vendaval.core.domain.model.PhoneNumber
import mx.vendaval.core.domain.service.ExternalLauncher
import mx.vendaval.core.domain.service.LocationOutcome
import mx.vendaval.core.domain.service.LocationProvider
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import java.awt.Desktop
import java.awt.datatransfer.StringSelection
import java.net.URI

/**
 * Desktop storage location, following each OS's convention rather than dumping a
 * dotfile in the home directory: `Library/Application Support` on macOS,
 * `%APPDATA%` on Windows, and the XDG data directory elsewhere.
 */
class DesktopStoragePaths : StoragePaths {
    override val fileSystem: FileSystem = FileSystem.SYSTEM

    override val root: Path = resolveRoot()

    private companion object {
        fun resolveRoot(): Path {
            val home = System.getProperty("user.home") ?: "."
            val os = (System.getProperty("os.name") ?: "").lowercase()
            val base = when {
                os.contains("mac") -> "$home/Library/Application Support"
                os.contains("win") -> System.getenv("APPDATA") ?: "$home/AppData/Roaming"
                else -> System.getenv("XDG_DATA_HOME") ?: "$home/.local/share"
            }
            return "$base/Vendaval".toPath()
        }
    }
}

/**
 * Desktop has no location services worth using.
 *
 * Reporting [LocationOutcome.Unsupported] rather than guessing from an IP
 * geolocation service is deliberate: a silently wrong "you are here" would
 * corrupt the distance ordering, and the UI instead offers an explicit reference
 * point (a municipality centre) which the user chooses knowingly.
 */
class DesktopLocationProvider : LocationProvider {
    override val isSupported: Boolean = false
    override suspend fun currentLocation(): LocationOutcome = LocationOutcome.Unsupported
}

/**
 * Opens links in the user's browser through AWT's [Desktop] integration.
 *
 * Phone numbers cannot be dialled from a desktop, so they are copied to the
 * clipboard instead — a useful action rather than a dead button.
 */
class DesktopExternalLauncher : ExternalLauncher {

    override fun openMap(space: CulturalSpace) = openUrl(space.googleMapsUrl)

    override fun dial(phone: PhoneNumber) {
        copyToClipboard(phone.display)
    }

    override fun openUrl(url: String) {
        runCatching {
            if (Desktop.isDesktopSupported() &&
                Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)
            ) {
                Desktop.getDesktop().browse(URI(url))
            }
        }
    }

    override fun share(space: CulturalSpace) {
        copyToClipboard("${space.name} · ${space.address}\n${space.googleMapsUrl}")
    }

    private fun copyToClipboard(text: String) {
        runCatching {
            java.awt.Toolkit.getDefaultToolkit().systemClipboard
                .setContents(StringSelection(text), null)
        }
    }
}
