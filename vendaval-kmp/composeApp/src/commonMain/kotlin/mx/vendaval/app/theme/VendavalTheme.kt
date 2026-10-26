package mx.vendaval.app.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Roles Material 3 has no slot for.
 *
 * Material covers `primary`, `surface` and friends but has nothing for "the
 * sunken well behind a search field", "the hairline on a card" or the five venue
 * hues. Carrying them here rather than scattering literals through composables
 * means light and dark stay in step and a designer has one file to edit.
 */
@Immutable
data class VendavalColors(
    val surfaceSunken: Color,
    val card: Color,
    val cardRaised: Color,
    val line: Color,
    val lineStrong: Color,
    val accent: Color,
    val accentGlow: Color,
    val textMuted: Color,
    val textFaint: Color,
    val warning: Color,
    val danger: Color,
    val isDark: Boolean,
)

/** Corner radii, matching the redesign's 12 / 17 / 22 rhythm. */
@Immutable
data class VendavalShapes(
    val small: Dp = 12.dp,
    val medium: Dp = 17.dp,
    val large: Dp = 22.dp,
)

private val DarkExtras = VendavalColors(
    surfaceSunken = Tokens.Ink2,
    card = Tokens.Card,
    cardRaised = Tokens.CardHi,
    line = Tokens.Line,
    lineStrong = Tokens.LineStrong,
    accent = Tokens.Accent,
    accentGlow = Tokens.AccentGlow,
    textMuted = Tokens.Muted,
    textFaint = Tokens.Faint,
    warning = Tokens.Warning,
    danger = Tokens.Danger,
    isDark = true,
)

private val LightExtras = VendavalColors(
    surfaceSunken = Tokens.PaperSunken,
    card = Tokens.PaperCard,
    cardRaised = Tokens.Paper,
    line = Tokens.PaperLine,
    lineStrong = Tokens.PaperLineStrong,
    accent = Tokens.AccentDeep,
    accentGlow = Color(0x14A8431F),
    textMuted = Tokens.InkMuted,
    textFaint = Tokens.InkFaint,
    warning = Color(0xFFB45309),
    danger = Color(0xFFC42B30),
    isDark = false,
)

/**
 * Dark scheme.
 *
 * `primary` is the accent and `onPrimary` is the ink, not white: at this
 * lightness the accent cannot carry white text, and a filled button is the one
 * place the pairing is load-bearing.
 */
private val DarkScheme = darkColorScheme(
    primary = Tokens.Accent,
    onPrimary = Tokens.Ink,
    primaryContainer = Color(0xFF3D1B0E),
    onPrimaryContainer = Tokens.Accent,
    secondary = Tokens.VenueCentro,
    onSecondary = Tokens.Ink,
    tertiary = Tokens.VenueGaleria,
    onTertiary = Tokens.Text,
    background = Tokens.Ink,
    onBackground = Tokens.Text,
    surface = Tokens.Card,
    onSurface = Tokens.Text,
    surfaceVariant = Tokens.CardHi,
    onSurfaceVariant = Tokens.Muted,
    surfaceContainerHighest = Tokens.CardHi,
    outline = Tokens.Faint,
    outlineVariant = Color(0xFF243044),
    error = Tokens.Danger,
    onError = Tokens.Text,
    scrim = Color(0xCC05080F),
)

private val LightScheme = lightColorScheme(
    primary = Tokens.AccentDeep,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFBE4DA),
    onPrimaryContainer = Color(0xFF5C2410),
    secondary = Color(0xFF3E6BC4),
    onSecondary = Color.White,
    tertiary = Color(0xFF7F4BC9),
    onTertiary = Color.White,
    background = Tokens.Paper,
    onBackground = Tokens.InkText,
    surface = Tokens.PaperCard,
    onSurface = Tokens.InkText,
    surfaceVariant = Tokens.PaperSunken,
    onSurfaceVariant = Tokens.InkMuted,
    surfaceContainerHighest = Tokens.PaperSunken,
    outline = Tokens.InkFaint,
    outlineVariant = Color(0xFFD6DCE5),
    error = Tokens.Danger,
    onError = Color.White,
    scrim = Color(0x99101828),
)

val LocalVendavalColors: ProvidableCompositionLocal<VendavalColors> =
    staticCompositionLocalOf { DarkExtras }

val LocalVendavalShapes: ProvidableCompositionLocal<VendavalShapes> =
    staticCompositionLocalOf { VendavalShapes() }

/** Shorthand mirroring `MaterialTheme.colorScheme`. */
object VendavalTheme {
    val colors: VendavalColors
        @Composable get() = LocalVendavalColors.current

    val radii: VendavalShapes
        @Composable get() = LocalVendavalShapes.current
}

@Composable
fun VendavalTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkScheme else LightScheme,
        typography = vendavalTypography(),
        shapes = vendavalMaterialShapes(),
    ) {
        CompositionLocalProvider(
            LocalVendavalColors provides if (darkTheme) DarkExtras else LightExtras,
            LocalVendavalShapes provides VendavalShapes(),
            content = content,
        )
    }
}
