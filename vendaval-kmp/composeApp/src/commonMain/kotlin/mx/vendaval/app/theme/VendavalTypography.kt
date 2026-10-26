package mx.vendaval.app.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import vendaval_kmp.composeapp.generated.resources.Res
import vendaval_kmp.composeapp.generated.resources.fraunces_variable
import vendaval_kmp.composeapp.generated.resources.inter_variable

/**
 * Type system.
 *
 * A serif display face paired with a neutral grotesque is what gives the app a
 * point of view. The previous revision declared "Space Grotesk + Plus Jakarta
 * Sans" in a comment while actually rendering `FontFamily.SansSerif` for
 * everything, which is precisely the anonymous look this redesign fixes.
 *
 * **Fraunces** carries the display voice: a high-contrast, slightly eccentric
 * serif that reads as editorial rather than corporate — fitting for a cultural
 * cartography project. **Inter** takes all body and UI text, where legibility at
 * 12-14sp matters more than personality.
 *
 * Both are variable fonts under the SIL Open Font License (~1.2 MB total, kept in
 * `licenses/`). Note that `compose-resources` 1.7 exposes no way to set variable
 * axes, so each file renders at its default instance and heavier weights are
 * synthesised. Hierarchy is therefore carried by **size and tracking** rather
 * than by weight — which is the more robust typographic move anyway, and avoids
 * shipping six static cuts per family.
 */
private val FrauncesFamily: FontFamily
    @Composable get() = FontFamily(
        Font(Res.font.fraunces_variable, FontWeight.Normal, FontStyle.Normal),
        Font(Res.font.fraunces_variable, FontWeight.SemiBold, FontStyle.Normal),
        Font(Res.font.fraunces_variable, FontWeight.Normal, FontStyle.Italic),
    )

private val InterFamily: FontFamily
    @Composable get() = FontFamily(
        Font(Res.font.inter_variable, FontWeight.Normal, FontStyle.Normal),
        Font(Res.font.inter_variable, FontWeight.Medium, FontStyle.Normal),
        Font(Res.font.inter_variable, FontWeight.SemiBold, FontStyle.Normal),
    )

@Composable
fun vendavalTypography(): Typography {
    val display = FrauncesFamily
    val body = InterFamily

    return Typography(
        // Hero. Negative tracking is what separates a display face from merely
        // oversized body copy.
        displayLarge = TextStyle(
            fontFamily = display, fontWeight = FontWeight.SemiBold,
            fontSize = 40.sp, lineHeight = 44.sp, letterSpacing = (-1.0).sp,
        ),
        displayMedium = TextStyle(
            fontFamily = display, fontWeight = FontWeight.SemiBold,
            fontSize = 31.sp, lineHeight = 35.sp, letterSpacing = (-0.6).sp,
        ),
        // Reserved for the second line of a two-part title, set in italic.
        displaySmall = TextStyle(
            fontFamily = display, fontWeight = FontWeight.Normal,
            fontStyle = FontStyle.Italic,
            fontSize = 31.sp, lineHeight = 35.sp, letterSpacing = (-0.6).sp,
        ),
        headlineLarge = TextStyle(
            fontFamily = display, fontWeight = FontWeight.SemiBold,
            fontSize = 25.sp, lineHeight = 30.sp, letterSpacing = (-0.4).sp,
        ),
        headlineMedium = TextStyle(
            fontFamily = display, fontWeight = FontWeight.SemiBold,
            fontSize = 20.sp, lineHeight = 25.sp, letterSpacing = (-0.3).sp,
        ),
        // Venue names in list rows: serif, because the name is the content.
        titleLarge = TextStyle(
            fontFamily = display, fontWeight = FontWeight.SemiBold,
            fontSize = 17.sp, lineHeight = 21.sp, letterSpacing = (-0.2).sp,
        ),
        titleMedium = TextStyle(
            fontFamily = display, fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp, lineHeight = 19.sp,
        ),
        bodyLarge = TextStyle(
            fontFamily = body, fontWeight = FontWeight.Normal,
            fontSize = 15.sp, lineHeight = 22.sp,
        ),
        bodyMedium = TextStyle(
            fontFamily = body, fontWeight = FontWeight.Normal,
            fontSize = 13.5.sp, lineHeight = 19.sp,
        ),
        bodySmall = TextStyle(
            fontFamily = body, fontWeight = FontWeight.Normal,
            fontSize = 12.sp, lineHeight = 16.sp,
        ),
        labelLarge = TextStyle(
            fontFamily = body, fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp, lineHeight = 17.sp, letterSpacing = 0.1.sp,
        ),
        labelMedium = TextStyle(
            fontFamily = body, fontWeight = FontWeight.Medium,
            fontSize = 12.sp, lineHeight = 16.sp,
        ),
        // Eyebrows and micro-labels: the one place wide tracking earns its keep.
        labelSmall = TextStyle(
            fontFamily = body, fontWeight = FontWeight.SemiBold,
            fontSize = 10.sp, lineHeight = 13.sp, letterSpacing = 1.6.sp,
        ),
    )
}

/** Oversized figures for the coverage screen. */
@Composable
fun statNumberStyle(): TextStyle = TextStyle(
    fontFamily = FrauncesFamily, fontWeight = FontWeight.SemiBold,
    fontSize = 34.sp, lineHeight = 38.sp, letterSpacing = (-1.2).sp,
    textAlign = TextAlign.Center,
)

/** Initial glyph inside a generated venue cover. */
@Composable
fun coverInitialStyle(): TextStyle = TextStyle(
    fontFamily = FrauncesFamily, fontWeight = FontWeight.SemiBold,
    fontSize = 30.sp, lineHeight = 32.sp, textAlign = TextAlign.Center,
)

@Composable
fun vendavalMaterialShapes(): Shapes = Shapes(
    extraSmall = RoundedCornerShape(9.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(17.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp),
)
