package mx.vendaval.app.feature.detail

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.NearMe
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import mx.vendaval.app.i18n.LocalStrings
import mx.vendaval.app.ui.photo.LocalPhotoStore
import mx.vendaval.app.theme.VendavalTheme
import mx.vendaval.app.theme.accentFor
import mx.vendaval.app.theme.coverInitialStyle
import mx.vendaval.app.ui.component.PendingUploadBadge
import mx.vendaval.core.domain.geo.formatDistance
import mx.vendaval.core.domain.model.SpaceListItem
import mx.vendaval.core.domain.model.SpaceOrigin
import mx.vendaval.core.domain.text.foldForSearch
import mx.vendaval.core.data.seed.VenuePhotoSeed
import mx.vendaval.core.domain.model.VenuePhoto
import mx.vendaval.core.domain.service.ExternalLauncher
import kotlin.math.abs

/**
 * Full record for one venue.
 *
 * Written as plain scrollable content, not as a sheet or a dialog, so the same
 * composable can be hosted in a bottom sheet on a phone and in a side pane on a
 * wide window. The host owns presentation; this owns content.
 *
 * ## Why it is shaped this way
 * Place-detail references (Beli, Zesty, Pangea Charging) converge on the same three
 * bands: a **full-bleed hero** that gives the record a face, a **row of equal-weight
 * quick actions** as icon-over-label stacks, and **metadata as a hairline-divided
 * list**. The previous version had none of them — it opened on a bare heading and
 * resolved into a `FlowRow` of one filled button and three outlined ones, which
 * wrapped unpredictably and implied a priority order that does not exist. Calling,
 * routing, sharing and saving are peers.
 */
@Composable
fun SpaceDetailContent(
    item: SpaceListItem,
    launcher: ExternalLauncher,
    onToggleFavourite: () -> Unit,
    onShowOnMap: () -> Unit,
    onDelete: () -> Unit,
    onClose: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val strings = LocalStrings.current
    val colours = VendavalTheme.colors
    val space = item.space

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
    ) {
        DetailHero(
            item = item,
            onToggleFavourite = onToggleFavourite,
            onClose = onClose,
        )

        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Spacer(Modifier.height(2.dp))

            // Sits directly under the image, the way a photo credit does in print.
            // Not optional: these photographs are CC BY-SA, which permits
            // redistribution only with attribution, so this row is the condition
            // for the hero above being allowed to exist.
            VenuePhotoSeed.forSpace(space.id)?.let { photo ->
                PhotoCredit(photo = photo, launcher = launcher)
            }

            QuickActions(
                item = item,
                launcher = launcher,
                onToggleFavourite = onToggleFavourite,
            )

            // Metadata as a single bounded block rather than loose rows. Grouping
            // them means the eye reads one object with fields, not four fragments.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(VendavalTheme.radii.medium))
                    .background(colours.card),
            ) {
                MetaRow(
                    icon = Icons.Outlined.Place,
                    label = strings.detailAddress,
                    value = space.address.ifBlank { space.municipality.label },
                )
                MetaDivider()
                MetaRow(
                    icon = Icons.Outlined.Call,
                    label = strings.detailPhone,
                    value = space.phone?.display ?: strings.detailNoPhone,
                    secondary = space.phone?.extension?.let(strings.detailExtension),
                    muted = space.phone == null,
                )
                item.distanceKm?.let { distance ->
                    MetaDivider()
                    MetaRow(
                        icon = Icons.Outlined.NearMe,
                        label = strings.mapNearMe,
                        value = formatDistance(distance),
                    )
                }
            }

            // Locating the venue inside the app is a navigation move, not a peer of
            // the quick actions, so it gets its own full-width row. A left-aligned
            // label with a trailing chevron reads as a deliberate destination — the
            // disclosure pattern the place-detail references use — rather than the
            // centred grey slab it was, which looked like an unlabelled default button.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(VendavalTheme.radii.medium))
                    .background(colours.cardRaised)
                    .clickable(role = Role.Button, onClick = onShowOnMap)
                    .padding(start = 16.dp, end = 12.dp, top = 14.dp, bottom = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = strings.detailShowOnMap,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    tint = colours.textFaint,
                    modifier = Modifier.size(20.dp),
                )
            }

            // Only device-local records are deletable; seeded and published entries
            // are shared data and are not this client's to remove.
            if (space.origin == SpaceOrigin.LOCAL) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = strings.detailLocalRecord,
                        style = MaterialTheme.typography.bodySmall,
                        color = colours.textFaint,
                    )
                    TextButton(onClick = onDelete) {
                        Icon(
                            Icons.Outlined.Delete,
                            contentDescription = null,
                            tint = colours.danger,
                        )
                        Text(
                            text = strings.detailDelete,
                            color = colours.danger,
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}

/**
 * Attribution for the hero photograph.
 *
 * Deliberately quiet — caption size, faint colour, one line — because it is a legal
 * requirement rather than something a visitor came here to read. But it is a real
 * link, not decoration: tapping it opens the file's page on Wikimedia Commons,
 * which is where the licence text and the full authorship live. Naming the author
 * and licence and pointing at the source is the whole of what CC BY-SA asks.
 */
@Composable
private fun PhotoCredit(
    photo: VenuePhoto,
    launcher: ExternalLauncher,
) {
    val strings = LocalStrings.current
    val colours = VendavalTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClickLabel = strings.photoCreditOpen) {
                launcher.openUrl(photo.sourceUrl)
            }
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.PhotoCamera,
            contentDescription = null,
            tint = colours.textFaint,
            modifier = Modifier.size(13.dp),
        )
        Text(
            text = strings.photoCredit(photo.credit),
            style = MaterialTheme.typography.labelSmall,
            color = colours.textFaint,
            maxLines = 2,
        )
    }
}

/**
 * The hero band.
 *
 * Shows the venue's photograph when one exists. About half the catalogue has none
 * — Wikimedia covers institutional heritage, not the independent scene — and
 * inventing images of real civic buildings would misrepresent them, so the
 * fallback is generated from the record itself: the type gradient with the venue's
 * initial set very large and very transparent as a watermark.
 *
 * The scrim at the base predates the photographs and turned out to be exactly what
 * they need too: it was there to hold the name's contrast over a mid-tone venue
 * hue, and it does the same job over a bright sky.
 */
@Composable
private fun DetailHero(
    item: SpaceListItem,
    onToggleFavourite: () -> Unit,
    onClose: (() -> Unit)?,
) {
    val strings = LocalStrings.current
    val colours = VendavalTheme.colors
    val space = item.space
    val accent = accentFor(space.type)

    val lean = remember(space.name) {
        0.2f + (abs(space.name.foldForSearch().hashCode()) % 60) / 100f
    }
    val initial = remember(space.name) {
        space.name.firstOrNull { it.isLetter() }?.uppercaseChar()?.toString()
    }

    val photoStore = LocalPhotoStore.current
    val credited = remember(space.id) { VenuePhotoSeed.forSpace(space.id) }
    val photo = credited?.let { photoStore?.photoOrLoad(it.assetPath) }
    val photoAlpha by animateFloatAsState(
        targetValue = if (photo != null) 1f else 0f,
        animationSpec = tween(durationMillis = 220),
        label = "heroPhoto",
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(196.dp)
            .background(
                Brush.linearGradient(
                    colors = listOf(accent.colour, accent.deep),
                    start = Offset.Zero,
                    end = Offset(1000f * lean, 900f),
                ),
            ),
    ) {
        // Oversized initial as a watermark. Clipped by the band on purpose — a
        // letter running off the edge reads as texture rather than as a label.
        // Suppressed under a photograph, where it would read as a defect.
        if (photo == null) {
            initial?.let {
                Text(
                    text = it,
                    style = coverInitialStyle().copy(fontSize = MaterialTheme.typography.displayLarge.fontSize * 3.4f),
                    color = Color.White.copy(alpha = 0.14f),
                    maxLines = 1,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 6.dp),
                )
            }
        }

        if (photo != null) {
            Image(
                bitmap = photo,
                contentDescription = null,   // the name below is already the heading
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize().alpha(photoAlpha),
            )
        }

        // Scrim so the name keeps its contrast over the mid-tone venue hues.
        Box(
            Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .height(132.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color(0xFF0A0E17).copy(alpha = 0.86f)),
                    ),
                ),
        )

        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp),
        ) {
            HeroIconButton(
                icon = if (item.isFavourite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                description = if (item.isFavourite) {
                    strings.detailFavouriteRemove
                } else {
                    strings.detailFavouriteAdd
                },
                tint = if (item.isFavourite) colours.warning else Color.White,
                onClick = onToggleFavourite,
            )
            onClose?.let {
                HeroIconButton(
                    icon = Icons.Outlined.Close,
                    description = strings.actionClose,
                    tint = Color.White,
                    onClick = it,
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 20.dp, end = 20.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                Icon(
                    imageVector = accent.icon,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.82f),
                    modifier = Modifier.size(15.dp),
                )
                Text(
                    text = space.type.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.82f),
                )
                PendingUploadBadge(space.origin)
            }
            Text(
                text = space.name,
                style = MaterialTheme.typography.displayMedium,
                color = Color.White,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun HeroIconButton(
    icon: ImageVector,
    description: String,
    tint: Color,
    onClick: () -> Unit,
) {
    IconButton(onClick = onClick) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.32f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = description,
                tint = tint,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

/**
 * Call, route, share, save — as equal-weight stacks.
 *
 * The call action is omitted rather than disabled when there is no number on
 * record: a button that cannot act is worse than an absent one, and the remaining
 * stacks redistribute evenly because each carries the same weight.
 */
@Composable
private fun QuickActions(
    item: SpaceListItem,
    launcher: ExternalLauncher,
    onToggleFavourite: () -> Unit,
) {
    val strings = LocalStrings.current
    val colours = VendavalTheme.colors
    val space = item.space

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        space.phone?.let { phone ->
            QuickAction(
                icon = Icons.Outlined.Call,
                label = strings.detailCall,
                modifier = Modifier.weight(1f),
            ) { launcher.dial(phone) }
        }
        QuickAction(
            icon = Icons.Outlined.Map,
            label = strings.detailActionMap,
            description = strings.detailOpenMap,
            modifier = Modifier.weight(1f),
        ) { launcher.openMap(space) }
        QuickAction(
            icon = Icons.Outlined.Share,
            label = strings.detailShare,
            modifier = Modifier.weight(1f),
        ) { launcher.share(space) }
        QuickAction(
            icon = if (item.isFavourite) Icons.Filled.Star else Icons.Outlined.StarBorder,
            label = if (item.isFavourite) strings.detailActionSaved else strings.detailActionSave,
            description = if (item.isFavourite) {
                strings.detailFavouriteRemove
            } else {
                strings.detailFavouriteAdd
            },
            tint = if (item.isFavourite) colours.warning else colours.accent,
            modifier = Modifier.weight(1f),
            onClick = onToggleFavourite,
        )
    }
}

@Composable
private fun QuickAction(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    tint: Color = VendavalTheme.colors.accent,
    onClick: () -> Unit,
) {
    val colours = VendavalTheme.colors
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(VendavalTheme.radii.medium))
            // `cardRaised`, not `card`. The detail pane is itself a card-level
            // surface, so card-on-card left these four stacks with no visible
            // container at all — they read as loose icons floating on the panel.
            .background(colours.cardRaised)
            .clickable(
                role = Role.Button,
                onClickLabel = description,
                onClick = onClick,
            )
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(21.dp),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = colours.textMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun MetaDivider() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(1.dp)
            .padding(start = 48.dp)
            .background(VendavalTheme.colors.line),
    )
}

@Composable
private fun MetaRow(
    icon: ImageVector,
    label: String,
    value: String,
    secondary: String? = null,
    muted: Boolean = false,
) {
    val colours = VendavalTheme.colors
    Row(
        modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colours.textFaint,
            modifier = Modifier.size(18.dp),
        )
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = colours.textFaint,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = if (muted) colours.textMuted else MaterialTheme.colorScheme.onSurface,
            )
            secondary?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = colours.textMuted,
                )
            }
        }
    }
}
