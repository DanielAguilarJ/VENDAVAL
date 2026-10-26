package mx.vendaval.app.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import mx.vendaval.app.i18n.LocalStrings
import mx.vendaval.app.theme.VendavalTheme
import mx.vendaval.app.theme.accentFor
import mx.vendaval.core.domain.geo.formatDistance
import mx.vendaval.core.domain.model.SpaceListItem

/**
 * One row of the directory — an editorial list item, not a card.
 *
 * ## Why this is not a card
 * The previous version wrapped every row in a rounded rectangle with its own
 * border and fill, floating on a background one step darker. That "each item in
 * its own box" treatment is the single strongest tell of generated UI: it reads
 * as a database dump dressed in Material defaults. Reference place-directory apps
 * (Beli, Zomato, Blue Bottle, Grab) do the opposite — rows sit directly on the
 * surface, anchored by a strong thumbnail and separated by a hairline inset to
 * the text. Whitespace and a single rule do the work a box was doing, and the
 * screen stops looking assembled and starts looking composed.
 *
 * ## Selection
 * A selected row gets a faint accent wash plus a 3 dp accent rule at its leading
 * edge — the way a mail client marks the open message — instead of a full
 * coloured border. The rule is reserved: the accent still means "this one",
 * it just no longer shouts it from four sides.
 *
 * The favourite toggle stays a real [IconButton] (48 dp target, own a11y node)
 * because the row itself is clickable; without the split a screen reader would
 * announce one control that does two different things.
 */
@Composable
fun SpaceCard(
    item: SpaceListItem,
    onClick: () -> Unit,
    onToggleFavourite: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    showDivider: Boolean = false,
) {
    val strings = LocalStrings.current
    val colours = VendavalTheme.colors
    val accent = accentFor(item.space.type)

    // Kept in sync so the divider lines up exactly with the text column start.
    val leadRule = 3.dp
    val gapBeforeCover = 11.dp
    val coverSize = 64.dp
    val gapAfterCover = 14.dp
    val dividerInset = leadRule + gapBeforeCover + coverSize + gapAfterCover

    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (isSelected) Modifier.background(accent.colour.copy(alpha = 0.08f)) else Modifier,
            )
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Leading rule: accent when selected, transparent otherwise so the
            // content never shifts between states.
            Box(
                Modifier
                    .width(leadRule)
                    .height(coverSize)
                    .background(if (isSelected) accent.colour else Color.Transparent),
            )
            Box(Modifier.width(gapBeforeCover))

            VenueCover(space = item.space, size = coverSize)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = gapAfterCover),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = item.space.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                // Municipality is the primary locator, distance the qualifier.
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.space.municipality.label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colours.textMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    item.distanceKm?.let { distance ->
                        Text(
                            text = "  ·  ${formatDistance(distance)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colours.textFaint,
                            maxLines = 1,
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Box(
                        Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(accent.colour),
                    )
                    Text(
                        text = item.space.type.label,
                        style = MaterialTheme.typography.bodySmall,
                        color = colours.textFaint,
                        maxLines = 1,
                    )
                    PendingUploadBadge(item.space.origin)
                }
            }

            IconButton(onClick = onToggleFavourite) {
                Icon(
                    imageVector = if (item.isFavourite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = if (item.isFavourite) {
                        strings.detailFavouriteRemove
                    } else {
                        strings.detailFavouriteAdd
                    },
                    tint = if (item.isFavourite) colours.warning else colours.textFaint,
                )
            }
        }

        // Hairline inset to the text column — the one separator an editorial list
        // needs. Drawn per row rather than as a heavy full-width rule.
        if (showDivider) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(start = dividerInset)
                    .height(1.dp)
                    .background(colours.line),
            )
        }
    }
}
