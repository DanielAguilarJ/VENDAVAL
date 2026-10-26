package mx.vendaval.app.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import mx.vendaval.app.theme.Tokens
import mx.vendaval.app.theme.VendavalTheme
import mx.vendaval.app.theme.markerColour
import mx.vendaval.core.domain.model.GeoBounds
import mx.vendaval.core.domain.model.SpaceListItem
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min

/**
 * Miniature constellation of the current result set.
 *
 * A cartography app whose main screen showed no cartography was the oddest gap in
 * the first revision. This is not the real map: it renders no tiles, makes no
 * network call and costs one `Canvas` pass. It plots the filtered venues in their
 * true relative positions so the shape of the result set — clustered downtown,
 * scattered across the state — is legible at a glance, and it is a large tap
 * target through to the real map.
 *
 * Positions come from a normalised [GeoBounds] over the visible items, so the
 * strip reframes itself as the filter narrows.
 */
@Composable
fun MapStrip(
    items: List<SpaceListItem>,
    onOpenMap: () -> Unit,
    modifier: Modifier = Modifier,
    /**
     * Announced to assistive technology, not drawn. The visible count sits above
     * the strip; painting it inside as well left the same figure on screen twice.
     */
    label: String,
) {
    val colours = VendavalTheme.colors

    // Normalised 0..1 positions plus colour, recomputed only when the set changes.
    // The spans travel alongside so the Canvas can preserve the geographic aspect
    // ratio rather than stretching each axis to fill the box.
    data class Plot(
        val points: List<Triple<Float, Float, Color>>,
        val spanLon: Double,
        val spanLat: Double,
        val centreLat: Double,
    )

    val plot = remember(items) {
        val bounds = GeoBounds.around(items.map { it.space.location }, paddingDegrees = 0.02)
            ?: return@remember Plot(emptyList(), 1.0, 1.0, 22.0)
        val spanLon = (bounds.east - bounds.west).takeIf { it > 1e-9 } ?: 1.0
        val spanLat = (bounds.north - bounds.south).takeIf { it > 1e-9 } ?: 1.0
        Plot(
            points = items.map { item ->
                val p = item.space.location
                Triple(
                    ((p.longitude - bounds.west) / spanLon).toFloat(),
                    // Latitude grows north, screen Y grows south.
                    (1.0 - (p.latitude - bounds.south) / spanLat).toFloat(),
                    markerColour(item.space.type),
                )
            },
            spanLon = spanLon,
            spanLat = spanLat,
            centreLat = bounds.centre.latitude,
        )
    }

    val plotted = plot.points
    val spanLon = plot.spanLon
    val spanLat = plot.spanLat
    val centreLatRad = plot.centreLat * PI / 180.0

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(88.dp)
            .clip(RoundedCornerShape(VendavalTheme.radii.medium))
            .background(
                Brush.linearGradient(
                    listOf(
                        if (colours.isDark) Color(0xFF16202F) else Color(0xFFE7EDF4),
                        if (colours.isDark) Color(0xFF0E1521) else Color(0xFFF4F7FA),
                    ),
                ),
            )
            .border(1.dp, colours.line, RoundedCornerShape(VendavalTheme.radii.medium))
            .clickable(onClick = onOpenMap)
            .semantics { contentDescription = label },
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val inset = 20f
            val boxW = size.width - inset * 2
            val boxH = size.height - inset * 2
            // Fit the geographic aspect ratio inside the box instead of stretching
            // each axis independently. Without this, a wide strip smears the
            // longitude span and the constellation no longer resembles the state —
            // pins pile up on one edge and the shape of the result set is lost.
            //
            // One degree of longitude is shorter than one of latitude by cos(lat);
            // at 22°N that is ~0.927, so the correction is small but visible.
            val lonScale = cos(centreLatRad).toFloat()
            val geoW = (spanLon.toFloat() * lonScale).coerceAtLeast(1e-6f)
            val geoH = spanLat.toFloat().coerceAtLeast(1e-6f)

            val scale = min(boxW / geoW, boxH / geoH)
            val drawW = geoW * scale
            val drawH = geoH * scale
            val originX = inset + (boxW - drawW) / 2f
            val originY = inset + (boxH - drawH) / 2f

            fun place(nx: Float, ny: Float) = Offset(originX + nx * drawW, originY + ny * drawH)

            // A faint graticule under everything. Twenty of the twenty-seven venues
            // sit in the capital, so most of this panel is legitimately empty — the
            // concentration is the finding, not a bug. But an empty dark rectangle
            // reads as "nothing loaded"; ruled space reads as territory, which is
            // what the emptiness actually means.
            val gridLine = if (colours.isDark) Color(0x12FFFFFF) else Color(0x14000000)
            for (i in 1..3) {
                val x = inset + boxW * i / 4f
                drawLine(gridLine, Offset(x, inset), Offset(x, inset + boxH), 1f)
            }
            for (i in 1..2) {
                val y = inset + boxH * i / 3f
                drawLine(gridLine, Offset(inset, y), Offset(inset + boxW, y), 1f)
            }

            // A soft halo per venue first, so dense clusters glow rather than
            // turning into an unreadable pile of hard dots.
            plotted.forEach { (nx, ny, colour) ->
                drawCircle(colour.copy(alpha = 0.16f), 22f, place(nx, ny))
            }
            plotted.forEach { (nx, ny, colour) ->
                val centre = place(nx, ny)
                drawCircle(Color(0xFF0A0E17).copy(alpha = 0.6f), 6.5f, centre)
                drawCircle(colour, 4.5f, centre)
            }
        }

    }
}
