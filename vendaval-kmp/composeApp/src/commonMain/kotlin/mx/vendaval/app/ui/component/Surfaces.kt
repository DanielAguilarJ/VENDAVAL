package mx.vendaval.app.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import mx.vendaval.app.theme.Tokens
import mx.vendaval.app.theme.VendavalTheme

/**
 * The translucent panel the original design uses everywhere.
 *
 * A real backdrop blur is not portable across the three targets, so the effect is
 * reproduced the way it actually reads on screen: a semi-transparent fill over the
 * ambient gradient, plus the 1 px hairline border that gives the glass its edge.
 * Cheaper than a blur and, unlike one, identical on every platform.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = VendavalTheme.radii.medium,
    highlighted: Boolean = false,
    content: @Composable BoxScope.() -> Unit,
) {
    val colours = VendavalTheme.colors
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(if (highlighted) colours.cardRaised else colours.card)
            .border(
                width = 1.dp,
                color = if (highlighted) colours.accent.copy(alpha = 0.35f) else colours.line,
                shape = RoundedCornerShape(cornerRadius),
            ),
        content = content,
    )
}

/**
 * A single neutral lift behind the content.
 *
 * This used to draw three coloured spheres — cyan, blue and violet — reproducing the
 * glow of the original web page. That glow was part of the look the redesign replaced:
 * it tinted every surface above it, so the warm surface ramp read as blue-grey, and it
 * put saturated colour behind text that had to stay legible. What remains is one very
 * faint off-centre lift, which keeps the background from being a dead flat field
 * without competing with anything drawn on top of it.
 *
 * Skipped entirely in light mode, where any lift turns into muddy tinting.
 */
@Composable
fun AmbientGlow(modifier: Modifier = Modifier) {
    if (!VendavalTheme.colors.isDark) return

    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                sphere(
                    colour = Tokens.CardHi,
                    centre = Offset(size.width * 0.22f, size.height * 0.06f),
                    radius = size.maxDimension * 0.85f,
                )
            },
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.sphere(
    colour: Color,
    centre: Offset,
    radius: Float,
) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(colour.copy(alpha = 0.16f), Color.Transparent),
            center = centre,
            radius = radius,
        ),
        radius = radius,
        center = centre,
    )
}
