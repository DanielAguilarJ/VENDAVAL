package mx.vendaval.app.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import mx.vendaval.app.theme.Tokens
import mx.vendaval.app.theme.accentFor
import mx.vendaval.app.theme.coverInitialStyle
import mx.vendaval.app.ui.photo.LocalPhotoStore
import mx.vendaval.core.data.seed.VenuePhotoSeed
import mx.vendaval.core.domain.model.CulturalSpace
import mx.vendaval.core.domain.text.foldForSearch
import kotlin.math.abs

/**
 * Cover for a venue: its photograph when one exists, a generated mark when it does not.
 *
 * ## Why this exists
 * Every well-designed place directory anchors each entry in an image — Google Maps,
 * Vrbo, Blue Bottle and Google Arts & Culture all place a square thumbnail at the
 * head of the row. Without some visual anchor the list reads as a database dump,
 * which was the single biggest problem with the first revision.
 *
 * Only about half the catalogue has a freely-licensed photograph (see
 * `VenuePhotoSeed`), and fabricating images of real civic buildings would
 * misrepresent them. So the generated mark is not a loading placeholder to be
 * replaced — it is the permanent answer for the venues Wikimedia has never
 * photographed, and the photo is layered *on top* of it. That ordering is the
 * whole trick: the thumbnail never changes size, never reflows, and a row with no
 * photograph is indistinguishable from a finished one rather than looking broken.
 *
 * The generated mark is a diagonal gradient in the venue type's hue, a soft
 * specular highlight, and the venue's initial set in the display serif. Because it
 * is derived from the record itself, a venue looks the same on every launch and on
 * every device, so users build recognition the same way they would with a photo.
 *
 * The gradient angle is jittered per venue so two Casas de Cultura sitting next
 * to each other are still distinguishable at a glance.
 */
@Composable
fun VenueCover(
    space: CulturalSpace,
    modifier: Modifier = Modifier,
    size: Dp = 74.dp,
    cornerRadius: Dp = 13.dp,
) {
    val accent = accentFor(space.type)

    // Resolved here rather than passed in: see LocalPhotoStore for why. Null at
    // every step is normal — no manifest entry, no store, or still downloading —
    // and each one lands on the generated cover below.
    val photoStore = LocalPhotoStore.current
    val credited = remember(space.id) { VenuePhotoSeed.forSpace(space.id) }
    val photo = credited?.let { photoStore?.photoOrLoad(it.assetPath) }

    // Fades in over the generated cover instead of appearing instantly. A hard
    // swap reads as a glitch when several arrive at once during a scroll.
    val photoAlpha by animateFloatAsState(
        targetValue = if (photo != null) 1f else 0f,
        animationSpec = tween(durationMillis = PHOTO_FADE_MS),
        label = "venuePhoto",
    )

    // Two venues of the same type sat side by side were rendering as visual
    // duplicates: the type hue is shared, and `Aquelarre` / `Artefacto` even share
    // an initial. So three independent axes are derived from the name — all stable
    // across launches, none of them the hue itself, so the type stays readable.
    val identity = remember(space.name) {
        val h = abs(space.name.foldForSearch().hashCode())
        CoverIdentity(
            // Gradient direction. A wide spread so neighbours differ obviously.
            lean = 0.18f + (h % 65) / 100f,          // 0.18 .. 0.82
            // Brightness shift on the light stop, ±14%.
            lift = ((h / 7) % 29 - 14) / 100f,
            // Hue rotation, ±16°. This is what actually separates two venues of the
            // same type. Lightness alone proved invisible at 74 dp: four Centro
            // Culturales in a row all read as the same blue square. Sixteen degrees
            // is wide enough to distinguish neighbours at a glance and narrow enough
            // that blue stays blue — the hue still says "Centro Cultural", it just
            // stops claiming these are the same record.
            hueShift = ((h / 11) % 33 - 16).toFloat(),
            // Where the specular highlight sits.
            glintX = 0.14f + ((h / 13) % 40) / 100f, // 0.14 .. 0.53
            glintY = 0.06f + ((h / 29) % 26) / 100f, // 0.06 .. 0.31
        )
    }

    val initial = remember(space.name) { displayInitial(space.name) }
    val stops = remember(accent, identity) {
        listOf(
            accent.colour.rotateHue(identity.hueShift).shift(identity.lift),
            accent.deep.rotateHue(identity.hueShift).shift(identity.lift * 0.5f),
        )
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
            .drawBehind {
                drawRect(
                    Brush.linearGradient(
                        colors = stops,
                        start = Offset(this.size.width * (1f - identity.lean), 0f),
                        end = Offset(this.size.width * identity.lean, this.size.height),
                    ),
                )
                // Specular highlight. Gives the flat fill a sense of material
                // without resorting to the glassmorphism the redesign removed.
                drawRect(
                    Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = 0.26f), Color.Transparent),
                        center = Offset(
                            this.size.width * identity.glintX,
                            this.size.height * identity.glintY,
                        ),
                        radius = this.size.maxDimension * 0.8f,
                    ),
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        if (initial != null) {
            Text(
                text = initial,
                style = coverInitialStyle(),
                // Dark ink on the hue rather than white: the hues are mid-tone,
                // and dark type on them holds contrast where white would not.
                color = Tokens.Ink.copy(alpha = 0.82f),
                modifier = Modifier.clearAndSetSemantics { },
            )
        } else {
            // No usable letter (a name that is all punctuation or digits) —
            // fall back to the type icon rather than showing an empty tile.
            Icon(
                imageVector = accent.icon,
                contentDescription = null,
                tint = Tokens.Ink.copy(alpha = 0.75f),
                modifier = Modifier.size(size / 2.6f),
            )
        }

        // Cropped, not fitted: the manifest holds portraits and landscapes alike,
        // and letterboxing them into a square would put bars of dead colour in the
        // list. The subject of a building photograph is centred often enough that a
        // centre crop is the right default.
        if (photo != null) {
            Image(
                bitmap = photo,
                contentDescription = null,   // the venue name is already the row's label
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize().alpha(photoAlpha),
            )
        }
    }
}

/** Crossfade for an arriving photograph. Matches the nav's 180 ms motion. */
private const val PHOTO_FADE_MS = 180

/** The per-venue variations that keep same-type covers distinguishable. */
private data class CoverIdentity(
    val lean: Float,
    val lift: Float,
    val hueShift: Float,
    val glintX: Float,
    val glintY: Float,
)

/**
 * Rotates a colour's hue by [degrees], leaving saturation and value alone.
 *
 * Done through HSV rather than an RGB matrix approximation because the venue hues are
 * already fairly saturated, and a matrix rotation desaturates visibly at that end of
 * the gamut — the covers would drift towards grey as they rotated.
 */
private fun Color.rotateHue(degrees: Float): Color {
    if (degrees == 0f) return this

    val max = maxOf(red, green, blue)
    val min = minOf(red, green, blue)
    val delta = max - min
    if (delta == 0f) return this      // grey has no hue to rotate

    val hue = when (max) {
        red -> 60f * (((green - blue) / delta) % 6f)
        green -> 60f * (((blue - red) / delta) + 2f)
        else -> 60f * (((red - green) / delta) + 4f)
    }
    val saturation = if (max == 0f) 0f else delta / max

    var h = (hue + degrees) % 360f
    if (h < 0f) h += 360f

    val c = max * saturation
    val x = c * (1f - kotlin.math.abs((h / 60f) % 2f - 1f))
    val m = max - c

    val (r, g, b) = when ((h / 60f).toInt()) {
        0 -> Triple(c, x, 0f)
        1 -> Triple(x, c, 0f)
        2 -> Triple(0f, c, x)
        3 -> Triple(0f, x, c)
        4 -> Triple(x, 0f, c)
        else -> Triple(c, 0f, x)
    }

    return Color(red = r + m, green = g + m, blue = b + m, alpha = alpha)
}

/** Lightens or darkens towards white/black without leaving the hue. */
private fun Color.shift(amount: Float): Color = if (amount >= 0f) {
    Color(
        red = red + (1f - red) * amount,
        green = green + (1f - green) * amount,
        blue = blue + (1f - blue) * amount,
        alpha = alpha,
    )
} else {
    val k = 1f + amount
    Color(red = red * k, green = green * k, blue = blue * k, alpha = alpha)
}

/**
 * First letter worth showing.
 *
 * Skips leading articles and generic prefixes so `Casa de Cultura de Calvillo`
 * reads as **C** but `Teatro Morelos` reads as **T**, and a name that begins with
 * a quotation mark still finds its letter. Returns `null` when nothing
 * alphabetic exists, so the caller can fall back to an icon.
 */
private fun displayInitial(name: String): String? {
    val skip = setOf("el", "la", "los", "las", "de", "del")
    val words = name.split(' ', '/', '“', '"', '(')
        .map { it.trim().trim('“', '”', '"', '\'', '(', ')', '.', ',') }
        .filter { it.isNotEmpty() }

    val chosen = words.firstOrNull { it.foldForSearch() !in skip && it.first().isLetter() }
        ?: words.firstOrNull { it.first().isLetter() }
        ?: return null

    return chosen.first().uppercaseChar().toString()
}
