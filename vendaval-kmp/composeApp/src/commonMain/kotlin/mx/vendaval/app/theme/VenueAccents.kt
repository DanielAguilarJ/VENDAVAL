package mx.vendaval.app.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Brush
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Museum
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.TheaterComedy
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import mx.vendaval.core.domain.model.VenueType

/**
 * Visual identity per venue type: hue, darker partner hue, and an icon.
 *
 * The hues descend from the web map's marker colours, so a venue is recognisably
 * the same thing on the portal and in the app — the colour, not the label, is
 * what a user reads off a dense map. Each is desaturated one step from the web
 * value because those were tuned for 8 px dots, not for 74 dp covers and type.
 *
 * Colour is never the only channel: every surface that shows a [VenueType] also
 * carries the icon and the written label, so the encoding survives
 * colour-blindness and greyscale printing.
 */
data class VenueAccent(
    val colour: Color,
    val deep: Color,
    val icon: ImageVector,
) {
    /**
     * Two stops for a generated cover.
     *
     * Covers exist because the catalogue has no photography and inventing photos
     * of real civic buildings would be dishonest. A deterministic gradient plus
     * the venue's initial gives every entry a stable, distinct visual anchor —
     * the same solution Notion and Linear use for avatars — which is what turns
     * the directory from a table of text into something browsable.
     */
    val coverStops: List<Color> get() = listOf(colour, deep)
}

private fun accentOf(type: VenueType): VenueAccent = when (type) {
    VenueType.CASA_DE_CULTURA -> VenueAccent(
        Tokens.VenueCasa, Tokens.VenueCasaDeep, Icons.Outlined.AccountBalance,
    )
    VenueType.CENTRO_CULTURAL -> VenueAccent(
        Tokens.VenueCentro, Tokens.VenueCentroDeep, Icons.Outlined.Museum,
    )
    VenueType.GALERIA_DE_ARTE -> VenueAccent(
        Tokens.VenueGaleria, Tokens.VenueGaleriaDeep, Icons.Outlined.Brush,
    )
    VenueType.TEATRO -> VenueAccent(
        Tokens.VenueTeatro, Tokens.VenueTeatroDeep, Icons.Outlined.TheaterComedy,
    )
    VenueType.CENTRO_COMUNITARIO -> VenueAccent(
        Tokens.VenueComunitario, Tokens.VenueComunitarioDeep, Icons.Outlined.Groups,
    )
    VenueType.OTRO -> VenueAccent(
        Tokens.VenueOtro, Tokens.VenueOtroDeep, Icons.Outlined.Place,
    )
}

/**
 * Theme-aware accent.
 *
 * On light surfaces the hues are darkened: a mid-tone saturated colour that reads
 * correctly on near-black fails contrast for a label on white.
 */
@Composable
fun accentFor(type: VenueType): VenueAccent {
    val base = accentOf(type)
    if (VendavalTheme.colors.isDark) return base
    return base.copy(
        colour = base.colour.darkenBy(0.28f),
        deep = base.deep.darkenBy(0.18f),
    )
}

/** Marker colour for the map layer, which draws outside any theming context. */
fun markerColour(type: VenueType): Color = accentOf(type).colour

/** Cover gradient stops, usable from non-composable draw code. */
fun coverStops(type: VenueType): List<Color> = accentOf(type).coverStops

private fun Color.darkenBy(fraction: Float): Color = Color(
    red = red * (1f - fraction),
    green = green * (1f - fraction),
    blue = blue * (1f - fraction),
    alpha = alpha,
)
