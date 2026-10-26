package mx.vendaval.app.feature.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CenterFocusStrong
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import mx.vendaval.app.i18n.LocalStrings
import mx.vendaval.app.presentation.CatalogueStateHolder
import mx.vendaval.app.presentation.LocationStatus
import mx.vendaval.app.theme.VendavalTheme
import mx.vendaval.app.theme.accentFor
import mx.vendaval.app.ui.component.VenueCover
import mx.vendaval.app.ui.map.MapCameraState
import mx.vendaval.app.ui.map.OsmMap
import mx.vendaval.app.ui.map.TileStore
import mx.vendaval.app.ui.map.rememberMapCameraState
import mx.vendaval.core.domain.geo.formatDistance
import mx.vendaval.core.domain.model.GeoBounds
import mx.vendaval.core.domain.model.SpaceId
import mx.vendaval.core.domain.model.SpaceListItem
import mx.vendaval.core.domain.model.VenueType

/**
 * Full-bleed map over the current query.
 *
 * The map shows exactly what the directory shows — same filter, same result set — so
 * narrowing the search in one view narrows the other. That coupling is why both read
 * from a single [CatalogueStateHolder] instead of each holding its own filter.
 *
 * ## Why the chrome moved
 * Reference map UIs (Journal, Viator, Zesty, CAVA) share three traits the previous
 * version lacked: controls **grouped into one floating unit** instead of a column of
 * loose circles, secondary information **collapsed behind a pill** so the map keeps
 * its area, and a **purpose-built bottom sheet** for the selection rather than the
 * directory row dropped on top of the tiles. A list row on a map reads as a stray
 * card; a sheet reads as part of the map.
 */
@Composable
fun MapScreen(
    catalogue: CatalogueStateHolder,
    tileStore: TileStore,
    selectedId: SpaceId?,
    onSelect: (SpaceId?) -> Unit,
    onOpenDetail: (SpaceId) -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalStrings.current
    val items by catalogue.visibleItems.collectAsState()
    val locationStatus by catalogue.locationStatus.collectAsState()

    val camera = rememberMapCameraState()
    val selectedItem = remember(items, selectedId) {
        items.firstOrNull { it.id == selectedId }
    }

    // Recentre when the visible set changes shape, but never while the user has a
    // venue selected — yanking the camera out from under a selection is disorienting.
    LaunchedEffect(items.size) {
        if (selectedId != null || items.isEmpty()) return@LaunchedEffect
        GeoBounds.around(items.map { it.space.location })?.let { bounds ->
            camera.fitTo(bounds, Size(1000f, 1000f))
        }
    }

    LaunchedEffect(selectedId) {
        selectedItem?.let { camera.moveTo(it.space.location, maxOf(camera.zoom, 15f)) }
    }

    Box(modifier = modifier.fillMaxSize()) {
        OsmMap(
            items = items,
            camera = camera,
            tileStore = tileStore,
            selectedId = selectedId,
            onMarkerClick = onSelect,
            userLocation = (locationStatus as? LocationStatus.Resolved)?.point,
            modifier = Modifier.fillMaxSize(),
        )

        // Scrim at the very top. OSM tiles are light and busy; without it the
        // floating pills lose their edges over pale streets.
        Box(
            Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(96.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Black.copy(alpha = 0.45f), Color.Transparent),
                    ),
                ),
        )

        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top,
        ) {
            CountPill(text = strings.resultCount(items.size))
            Spacer(Modifier.weight(1f))
            MapLegend()
        }

        MapControlCluster(
            camera = camera,
            showLocateButton = catalogue.supportsLocation,
            onLocate = catalogue::requestLocation,
            onRecentre = {
                GeoBounds.around(items.map { it.space.location })?.let { bounds ->
                    camera.fitTo(bounds, Size(1000f, 1000f))
                }
            },
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp),
        )

        // Selected-venue preview: tapping a marker gives the essentials in place,
        // and only a second tap commits to the full detail view.
        AnimatedVisibility(
            visible = selectedItem != null,
            enter = slideInVertically(tween(220)) { it },
            exit = slideOutVertically(tween(180)) { it },
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            selectedItem?.let { item ->
                MapSelectionSheet(
                    item = item,
                    onOpenDetail = { onOpenDetail(item.id) },
                    onToggleFavourite = { catalogue.onToggleFavourite(item.id) },
                    onDismiss = { onSelect(null) },
                )
            }
        }

        (locationStatus as? LocationStatus.Rejected)?.let {
            LocationNotice(
                message = when {
                    !catalogue.supportsLocation -> strings.mapLocationUnsupported
                    else -> strings.mapLocationDenied
                },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 62.dp, start = 12.dp, end = 12.dp),
            )
        }
    }
}

/**
 * The selection sheet.
 *
 * Anchored to the bottom edge with a squared-off base, so it reads as attached to
 * the screen rather than floating over it — the cue every reference map UI uses to
 * say "this belongs to the map below".
 */
@Composable
private fun MapSelectionSheet(
    item: SpaceListItem,
    onOpenDetail: () -> Unit,
    onToggleFavourite: () -> Unit,
    onDismiss: () -> Unit,
) {
    val strings = LocalStrings.current
    val colours = VendavalTheme.colors
    val accent = accentFor(item.space.type)
    val shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colours.card)
            .padding(top = 8.dp, bottom = 18.dp),
    ) {
        // Grab handle. Purely a signifier — the sheet is dismissed by the close
        // button, not by dragging — but its absence made the panel read as a card.
        Box(
            Modifier
                .align(Alignment.CenterHorizontally)
                .width(38.dp)
                .height(4.dp)
                .clip(CircleShape)
                .background(colours.lineStrong),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 14.dp, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            VenueCover(space = item.space, size = 58.dp)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 13.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = item.space.name,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(accent.colour),
                    )
                    Text(
                        text = buildString {
                            append("  ")
                            append(item.space.type.label)
                            append("  ·  ")
                            append(item.space.municipality.label)
                            item.distanceKm?.let { append("  ·  ${formatDistance(it)}") }
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = colours.textMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
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
            IconButton(onClick = onDismiss) {
                Icon(
                    Icons.Outlined.Close,
                    contentDescription = strings.actionClose,
                    tint = colours.textFaint,
                )
            }
        }

        // One committed action, full width — a solid, brand-accent button. The
        // previous fill was the venue-type hue at 16% alpha, which read as a default
        // tonal chip and, worse, changed colour per venue. A single confident CTA in
        // the reserved interaction accent is what the reference peek cards resolve to.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp)
                .clip(RoundedCornerShape(VendavalTheme.radii.medium))
                .background(colours.accent)
                .clickable(role = Role.Button, onClick = onOpenDetail)
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = strings.mapOpenDetail,
                style = MaterialTheme.typography.bodyLarge,
                color = colours.surfaceSunken,
            )
        }
    }
}

/**
 * Zoom, recentre and locate as one grouped slab.
 *
 * Four detached circles read as four unrelated affordances and cost four separate
 * hit-target judgements. Grouping them — with a hairline between the zoom pair and
 * the navigation pair — matches the platform convention and halves the visual noise.
 */
@Composable
private fun MapControlCluster(
    camera: MapCameraState,
    showLocateButton: Boolean,
    onLocate: () -> Unit,
    onRecentre: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalStrings.current
    val colours = VendavalTheme.colors

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(VendavalTheme.radii.medium))
            .background(colours.card.copy(alpha = 0.94f)),
    ) {
        ClusterButton(Icons.Outlined.Add, strings.mapZoomIn) { camera.zoomBy(1f) }
        ClusterDivider()
        ClusterButton(Icons.Outlined.Remove, strings.mapZoomOut) { camera.zoomBy(-1f) }
        ClusterDivider()
        ClusterButton(Icons.Outlined.CenterFocusStrong, strings.mapRecentre, onRecentre)
        if (showLocateButton) {
            ClusterDivider()
            ClusterButton(Icons.Outlined.MyLocation, strings.mapNearMe, onLocate)
        }
    }
}

@Composable
private fun ClusterDivider() {
    Box(
        Modifier
            .width(46.dp)
            .height(1.dp)
            .background(VendavalTheme.colors.line),
    )
}

@Composable
private fun ClusterButton(icon: ImageVector, description: String, onClick: () -> Unit) {
    IconButton(onClick = onClick, modifier = Modifier.size(46.dp)) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = VendavalTheme.colors.textMuted,
            modifier = Modifier.size(19.dp),
        )
    }
}

/** Result count, so the map answers "how many" without deferring to the list. */
@Composable
private fun CountPill(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
            .clip(CircleShape)
            .background(VendavalTheme.colors.card.copy(alpha = 0.94f))
            .padding(horizontal = 13.dp, vertical = 8.dp),
    )
}

/**
 * Colour key for the markers, collapsed by default.
 *
 * Hue is the map's only type channel, so the key has to exist — but it is reference
 * material, consulted once. Leaving all six rows permanently open spent a corner of
 * the map on something already learned, so it now opens on demand.
 */
@Composable
private fun MapLegend() {
    var expanded by remember { mutableStateOf(false) }
    val colours = VendavalTheme.colors
    val strings = LocalStrings.current

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(VendavalTheme.radii.medium))
            .background(colours.card.copy(alpha = 0.94f))
            .clickable(role = Role.Button) { expanded = !expanded }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = strings.mapLegend,
            style = MaterialTheme.typography.labelSmall,
            color = colours.textMuted,
        )

        if (expanded) {
            VenueType.selectable.forEach { type ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    Box(
                        Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(accentFor(type).colour),
                    )
                    Text(
                        text = type.shortLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun LocationNotice(message: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(VendavalTheme.radii.medium))
            .background(Color.Black.copy(alpha = 0.78f))
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White,
        )
    }
}
