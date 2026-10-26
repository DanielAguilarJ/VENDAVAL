package mx.vendaval.app.feature.coverage

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.vendaval.app.i18n.LocalStrings
import mx.vendaval.app.presentation.CatalogueStateHolder
import mx.vendaval.app.theme.VendavalTheme
import mx.vendaval.app.theme.accentFor
import mx.vendaval.app.ui.component.AnimatedCounter

/**
 * Coverage statistics.
 *
 * Replaces the three static counters in the original hero, which had drifted from the
 * data they claimed to describe. Everything here is derived from the live catalogue,
 * and the per-municipality breakdown deliberately includes the four municipalities
 * with zero venues — the gaps are the most useful thing on this screen for a
 * collective trying to decentralise access to culture.
 *
 * ## Why the hierarchy changed
 * Analytics references (Linktree, Squarespace) lead with **one dominant figure** and
 * demote everything else to supporting text. The previous layout gave five equal
 * tiles equal weight, which is the visual equivalent of reading a list in monotone:
 * nothing was primary, so the screen had no answer to "how are we doing?". Now the
 * catalogue size leads, the state-coverage ratio is the one derived figure that gets
 * its own meter, and the rest are compact inline pairs.
 */
@Composable
fun CoverageScreen(
    catalogue: CatalogueStateHolder,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(16.dp),
) {
    val strings = LocalStrings.current
    val colours = VendavalTheme.colors
    val stats by catalogue.coverage.collectAsState()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item(key = "headline") {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = strings.coverageTitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = colours.textFaint,
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    AnimatedCounter(
                        target = stats.totalSpaces,
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 68.sp,
                            lineHeight = 74.sp,
                        ),
                    )
                    Text(
                        text = strings.coverageSpaces,
                        style = MaterialTheme.typography.bodyLarge,
                        color = colours.textMuted,
                        modifier = Modifier.padding(start = 10.dp, bottom = 12.dp),
                    )
                }
            }
        }

        // The state-coverage ratio is the only figure here that carries a target, so
        // it is the only one drawn as a meter. A meter implies "out of", and none of
        // the other counts have a denominator to be out of.
        item(key = "reach") {
            CoverageMeter(
                covered = stats.municipalitiesCovered,
                total = stats.municipalitiesInState,
                label = strings.coverageMunicipalities,
            )
        }

        item(key = "secondary") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(VendavalTheme.radii.medium))
                    .background(colours.card)
                    .padding(vertical = 14.dp),
            ) {
                InlineStat(stats.distinctTypes, strings.coverageTypes, Modifier.weight(1f))
                StatSeparator()
                InlineStat(stats.withPhone, strings.coverageWithPhone, Modifier.weight(1f))
                if (stats.localContributions > 0) {
                    StatSeparator()
                    InlineStat(
                        value = stats.localContributions,
                        label = strings.coverageContributions,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        item(key = "by-type") {
            if (stats.byType.isEmpty()) {
                BreakdownCard(strings.coverageByType) {
                    Text(
                        text = strings.coverageNoData,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colours.textFaint,
                    )
                }
            } else {
                val maxByType = stats.byType.values.maxOrNull() ?: 1
                BreakdownCard(strings.coverageByType) {
                    stats.byType.forEach { (type, count) ->
                        BarRow(
                            label = type.label,
                            value = count,
                            fraction = count.toFloat() / maxByType,
                            colour = accentFor(type).colour,
                        )
                    }
                }
            }
        }

        item(key = "by-municipality") {
            val maxByMunicipality = stats.byMunicipality.values.maxOrNull()?.coerceAtLeast(1) ?: 1
            BreakdownCard(strings.coverageByMunicipality) {
                stats.byMunicipality
                    .entries
                    .sortedByDescending { it.value }
                    .forEach { (municipality, count) ->
                        BarRow(
                            label = municipality.label,
                            value = count,
                            fraction = count.toFloat() / maxByMunicipality,
                            // Neutral, not accent. Tokens.kt reserves the accent for
                            // "you can interact with this"; painting eleven ordinary
                            // data bars in it drained the word of meaning and left
                            // the coverage meter — the one figure on this screen that
                            // is genuinely an achievement — with nothing to stand out
                            // against. Zero-count municipalities dim further still.
                            colour = if (count == 0) colours.line else colours.textMuted,
                        )
                    }
            }
        }

        item(key = "spacer") {
            Spacer(Modifier.height(24.dp))
        }
    }
}

/**
 * How much of the state the catalogue reaches.
 *
 * Drawn as segments rather than a continuous bar: there are eleven municipalities,
 * a countable number, and eleven discrete blocks let you see at a glance that four
 * are empty. A continuous bar at 64 % would hide which four.
 */
@Composable
private fun CoverageMeter(covered: Int, total: Int, label: String) {
    val colours = VendavalTheme.colors
    val safeTotal = total.coerceAtLeast(1)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(VendavalTheme.radii.medium))
            .background(colours.card)
            .padding(horizontal = 14.dp, vertical = 14.dp)
            .semantics { contentDescription = "$label: $covered / $total" },
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = covered.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = colours.accent,
                )
                Text(
                    text = " / $total",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colours.textFaint,
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clearAndSetSemantics { },
            horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            repeat(safeTotal) { index ->
                Box(
                    Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (index < covered) colours.accent else colours.line),
                )
            }
        }
    }
}

@Composable
private fun InlineStat(value: Int, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        AnimatedCounter(
            target = value,
            style = MaterialTheme.typography.headlineLarge,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = VendavalTheme.colors.textFaint,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun StatSeparator() {
    Box(
        Modifier
            .width(1.dp)
            .height(34.dp)
            .background(VendavalTheme.colors.line),
    )
}

/** One titled block of bars. Grouping keeps a long breakdown from reading as a dump. */
@Composable
private fun BreakdownCard(title: String, content: @Composable () -> Unit) {
    val colours = VendavalTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(VendavalTheme.radii.medium))
            .background(colours.card)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = colours.textFaint,
        )
        content()
    }
}

/**
 * One horizontal bar.
 *
 * The bar is decorative for assistive technology — the row carries a single
 * content description with the label and the count, so a screen reader reads
 * "Teatro, 4" instead of announcing an unlabelled graphic.
 */
@Composable
private fun BarRow(
    label: String,
    value: Int,
    fraction: Float,
    colour: Color,
    modifier: Modifier = Modifier,
) {
    val animated by animateFloatAsState(
        targetValue = fraction.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 700),
        label = "bar",
    )
    val colours = VendavalTheme.colors

    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = "$label, $value" },
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                modifier = Modifier.weight(1f, fill = false),
            ) {
                Box(Modifier.size(7.dp).clip(CircleShape).background(colour))
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    // A zero-count municipality is a gap, not a result. Dimming the
                    // label is what makes the gaps legible as a group.
                    color = if (value == 0) colours.textFaint else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = if (value == 0) colours.textFaint else colours.textMuted,
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(colours.line),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animated)
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(colour),
            )
        }
    }
}
