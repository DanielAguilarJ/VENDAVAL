package mx.vendaval.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.automirrored.outlined.ViewList
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import mx.vendaval.app.i18n.Strings
import mx.vendaval.core.domain.model.SpaceId

/** The five root destinations, shown in a bottom bar or a navigation rail. */
enum class TopLevelDestination(
    val icon: ImageVector,
    /**
     * The glyph for the live destination.
     *
     * Outline for the four you could go to, solid for the one you are on. This is
     * the mechanism nearly every surveyed reference (PayPal, Substack, Grab, Hulu,
     * Starbucks, Strava, Monzo) uses to mark the active tab, and it is what the nav
     * was missing: with a single icon per destination, "selected" could only ever be
     * a colour change, which is why the state had to shout in neon to register at
     * all. Weight carries it now, so the colour can stop shouting.
     */
    val selectedIcon: ImageVector,
    /**
     * Whether the destination should ignore the shell's reading-measure cap.
     *
     * True only for the map. Text screens are capped so a single column of cards
     * does not stretch across a 1280 px window, but applying that cap to the map
     * left a dead column of background beside the rail — a map has no line length
     * to protect and every pixel it is given is useful.
     */
    val isFullBleed: Boolean = false,
) {
    // Auto-mirrored so the list glyph flips correctly under a right-to-left locale.
    DIRECTORY(Icons.AutoMirrored.Outlined.ViewList, Icons.AutoMirrored.Filled.ViewList),
    MAP(Icons.Outlined.Map, Icons.Filled.Map, isFullBleed = true),
    // StarBorder, not Star: in Material's icon set the "Outlined" Star is a solid
    // glyph, so it sat in the nav row at a heavier weight than its four outlined
    // neighbours and read as a different icon family.
    FAVOURITES(Icons.Outlined.StarBorder, Icons.Filled.Star),
    COVERAGE(Icons.Outlined.BarChart, Icons.Filled.BarChart),
    SETTINGS(Icons.Outlined.Settings, Icons.Filled.Settings),
    ;

    fun label(strings: Strings): String = when (this) {
        DIRECTORY -> strings.navDirectory
        MAP -> strings.navMap
        FAVOURITES -> strings.navFavourites
        COVERAGE -> strings.navCoverage
        SETTINGS -> strings.navSettings
    }
}

/** Transient layers stacked over the current destination. */
@Immutable
sealed interface Overlay {
    data class Detail(val id: SpaceId) : Overlay
    data object AddSpace : Overlay
}

/**
 * Navigation state for the whole app.
 *
 * Modelled as a tab selection plus at most one overlay rather than as a generic
 * back stack, because that is what the app actually is: five peer views over one
 * dataset, with a detail layer that can present as a bottom sheet on a phone and
 * as a side pane on a desktop. A stack would add push/pop semantics that no screen
 * needs and would make the adaptive two-pane layout harder, not easier.
 *
 * Hand-rolled rather than using `navigation-compose`, whose multiplatform artifact
 * was still pre-release at the time of writing; this is ~40 lines, type-safe, and
 * has no alpha dependency.
 */
class AppNavigator {

    var destination: TopLevelDestination by mutableStateOf(TopLevelDestination.DIRECTORY)
        private set

    var overlay: Overlay? by mutableStateOf(null)
        private set

    /**
     * Detail selection survives a tab change so a venue opened from the list is
     * still highlighted after switching to the map — the two-pane layout on wide
     * screens depends on that.
     */
    var selectedSpaceId: SpaceId? by mutableStateOf(null)
        private set

    fun select(destination: TopLevelDestination) {
        if (this.destination == destination) return
        this.destination = destination
        // Overlays belong to the destination that opened them.
        if (overlay is Overlay.Detail) overlay = null
    }

    fun openDetail(id: SpaceId, asOverlay: Boolean) {
        selectedSpaceId = id
        overlay = if (asOverlay) Overlay.Detail(id) else null
    }

    fun closeDetail() {
        overlay = null
        selectedSpaceId = null
    }

    fun openAddSpace() {
        overlay = Overlay.AddSpace
    }

    fun closeOverlay() {
        overlay = null
    }

    fun showOnMap(id: SpaceId) {
        selectedSpaceId = id
        overlay = null
        destination = TopLevelDestination.MAP
    }

    /** Returns true when the event was consumed, so the platform can decide to exit. */
    fun handleBack(): Boolean = when {
        overlay != null -> {
            overlay = null
            true
        }

        destination != TopLevelDestination.DIRECTORY -> {
            destination = TopLevelDestination.DIRECTORY
            true
        }

        else -> false
    }
}
