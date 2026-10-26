package mx.vendaval.app.feature.directory

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NearMe
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import mx.vendaval.app.i18n.LocalStrings
import mx.vendaval.app.presentation.CatalogueStateHolder
import mx.vendaval.app.presentation.LocationStatus
import mx.vendaval.app.theme.VendavalTheme
import mx.vendaval.app.theme.accentFor
import mx.vendaval.app.ui.component.EmptyState
import mx.vendaval.app.ui.component.MapStrip
import mx.vendaval.app.ui.component.SearchField
import mx.vendaval.app.ui.component.SpaceCard
import mx.vendaval.app.ui.component.SyncBadge
import mx.vendaval.core.domain.model.Municipality
import mx.vendaval.core.domain.model.SortMode
import mx.vendaval.core.domain.model.SpaceId
import mx.vendaval.core.domain.model.VenueType

/**
 * The directory: search, filter, browse.
 *
 * ## What the restructure fixed
 * The first revision put seven blocks of chrome above the first venue — title,
 * three oversized stat counters, a search field, a full-width saturated CTA, two
 * chip rows and a sort row — so the content, which is the entire point, started
 * below the fold. And the eleven municipality chips overflowed the screen edge,
 * leaving `San Franc…` clipped with no way to reach the rest.
 *
 * Now: a two-line editorial title, one row pairing search with the (demoted) add
 * action, a single row of venue-type chips, and municipality and sort behind
 * progressive-disclosure toggles that expand in place. Counters moved to the
 * Coverage tab where they already lived. Content begins roughly one screen
 * earlier.
 */
@Composable
fun DirectoryScreen(
    catalogue: CatalogueStateHolder,
    onOpenSpace: (SpaceId) -> Unit,
    onAddSpace: () -> Unit,
    onOpenMap: () -> Unit,
    modifier: Modifier = Modifier,
    selectedId: SpaceId? = null,
    contentPadding: PaddingValues = PaddingValues(18.dp),
) {
    val strings = LocalStrings.current
    val colours = VendavalTheme.colors

    val items by catalogue.visibleItems.collectAsState()
    val filter by catalogue.filter.collectAsState()
    val sortMode by catalogue.sortMode.collectAsState()
    val syncState by catalogue.syncState.collectAsState()

    var municipalitiesOpen by rememberSaveable { mutableStateOf(false) }
    var sortOpen by rememberSaveable { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item(key = "masthead") {
            Column {
                Text(
                    text = "Colectivo Vendaval · IAJU",
                    style = MaterialTheme.typography.labelSmall,
                    color = colours.textFaint,
                )
                Spacer(Modifier.height(8.dp))
                // Split title: roman then italic. The tonal shift does the work a
                // second colour would otherwise have to do.
                Text(
                    text = "Cartografía cultural",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "de Aguascalientes",
                    style = MaterialTheme.typography.displaySmall,
                    color = colours.textMuted,
                )
                Spacer(Modifier.height(8.dp))
                SyncBadge(syncState)
            }
        }

        item(key = "search") {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                SearchField(
                    query = filter.query,
                    onQueryChange = catalogue::onQueryChange,
                    modifier = Modifier.weight(1f),
                )
                // Demoted from a full-width saturated button to a 46 dp affordance.
                // Adding a venue is a rare action and no longer deserves the
                // loudest colour on the screen.
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(VendavalTheme.radii.small))
                        .background(colours.card)
                        .border(1.dp, colours.line, RoundedCornerShape(VendavalTheme.radii.small))
                        .clickable(onClick = onAddSpace),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Outlined.Add,
                        contentDescription = strings.addSpace,
                        tint = colours.accent,
                    )
                }
            }
        }

        item(key = "type-chips") {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                item {
                    PillChip(
                        label = strings.filterAll,
                        selected = filter.types.isEmpty(),
                        onClick = catalogue::onClearTypes,
                    )
                }
                items(VenueType.selectable, key = { it.name }) { type ->
                    PillChip(
                        label = type.shortLabel,
                        selected = type in filter.types,
                        dotColour = accentFor(type).colour,
                        onClick = { catalogue.onToggleType(type) },
                    )
                }
            }
        }

        item(key = "disclosure") {
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                DisclosureChip(
                    label = strings.filterMunicipality,
                    badge = filter.municipalities.size,
                    expanded = municipalitiesOpen,
                    onClick = {
                        municipalitiesOpen = !municipalitiesOpen
                        if (municipalitiesOpen) sortOpen = false
                    },
                )
                DisclosureChip(
                    label = strings.sortBy,
                    badge = 0,
                    expanded = sortOpen,
                    onClick = {
                        sortOpen = !sortOpen
                        if (sortOpen) municipalitiesOpen = false
                    },
                )
            }
        }

        // Progressive disclosure: eleven municipalities wrap into a panel instead
        // of overflowing a single scroll row where most were unreachable.
        item(key = "municipality-panel") {
            AnimatedVisibility(visible = municipalitiesOpen) {
                Panel {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                        verticalArrangement = Arrangement.spacedBy(7.dp),
                    ) {
                        PillChip(
                            label = strings.filterAll,
                            selected = filter.municipalities.isEmpty(),
                            onClick = catalogue::onClearMunicipalities,
                        )
                        Municipality.entries.forEach { municipality ->
                            PillChip(
                                label = municipality.label,
                                selected = municipality in filter.municipalities,
                                onClick = { catalogue.onToggleMunicipality(municipality) },
                            )
                        }
                    }
                }
            }
        }

        item(key = "sort-panel") {
            AnimatedVisibility(visible = sortOpen) {
                Panel {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        SortMode.entries.forEach { mode ->
                            // Proximity is hidden where no reference point can
                            // exist, rather than offered and silently ignored.
                            val available = mode != SortMode.DISTANCE ||
                                catalogue.supportsLocation ||
                                catalogue.locationPoint != null
                            if (!available) return@forEach
                            PillChip(
                                label = mode.label,
                                selected = sortMode == mode,
                                onClick = { catalogue.onSortChange(mode) },
                            )
                        }
                    }
                }
            }
        }

        item(key = "nearby") {
            NearbyPrompt(
                catalogue = catalogue,
                sortMode = sortMode,
            )
        }

        item(key = "count") {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = strings.resultCount(items.size),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colours.textFaint,
                )
                if (filter.isActive) {
                    TextButton(onClick = catalogue::onClearFilters) {
                        Text(
                            text = "${strings.filterClear} (${filter.activeConstraintCount})",
                            style = MaterialTheme.typography.labelLarge,
                            color = colours.accent,
                        )
                    }
                }
            }
        }

        if (items.isNotEmpty()) {
            item(key = "map-strip") {
                MapStrip(
                    items = items,
                    onOpenMap = onOpenMap,
                    label = strings.resultCount(items.size),
                )
            }
        }

        if (items.isEmpty()) {
            item(key = "empty") {
                EmptyState(
                    icon = Icons.Outlined.SearchOff,
                    title = strings.noResultsTitle,
                    body = strings.noResultsBody,
                    action = {
                        if (filter.isActive) {
                            TextButton(onClick = catalogue::onClearFilters) {
                                Text(strings.filterClear)
                            }
                        }
                    },
                )
            }
        } else {
            // One item holds every row so this section owns its spacing: the
            // list's 10 dp gap still governs the header blocks above, while the
            // venue rows butt together and are separated by hairlines — an
            // editorial list rather than a stack of floating cards. 27 rows is
            // far too few for lazy virtualisation to matter.
            item(key = "venues") {
                Column(Modifier.fillMaxWidth()) {
                    items.forEachIndexed { index, item ->
                        SpaceCard(
                            item = item,
                            onClick = { onOpenSpace(item.id) },
                            onToggleFavourite = { catalogue.onToggleFavourite(item.id) },
                            isSelected = item.id == selectedId,
                            showDivider = index < items.lastIndex,
                        )
                    }
                }
            }
        }

        item(key = "tail") { Spacer(Modifier.height(20.dp)) }
    }
}

/** Container for a disclosure panel. */
/**
 * Offers to sort the directory by how close each space is.
 *
 * The machinery for this already existed and was invisible: `SpaceQueryEngine`
 * has taken a reference point since the beginning, `SortMode.DISTANCE` works, and
 * `SpaceCard` already prints a formatted distance. All of it was reachable only by
 * opening "Sort by" and picking an option whose value you cannot know until after
 * you pick it. So this is not new capability, it is capability made findable.
 *
 * Inline rather than a dialog, following Best Buy, Shake Shack and Monese, which
 * all put the ask in the list where the benefit is: a one-line reason and a single
 * action, ignorable by scrolling past. A permission sheet that interrupts before
 * you have seen the list cannot explain why it wants anything.
 *
 * It renders nothing in three cases, each deliberate:
 *
 * - the platform has no location provider, so the offer would be a lie;
 * - the list is already sorted by distance, so there is nothing left to offer;
 * - the user declined. One ask is a service, a second is nagging, and the option
 *   stays in the sort panel for anyone who changes their mind.
 */
@Composable
private fun NearbyPrompt(
    catalogue: CatalogueStateHolder,
    sortMode: SortMode,
) {
    val strings = LocalStrings.current
    val colours = VendavalTheme.colors
    val status by catalogue.locationStatus.collectAsState()

    if (!catalogue.supportsLocation) return
    if (status is LocationStatus.Rejected) return

    val resolved = status as? LocationStatus.Resolved
    if (resolved != null && sortMode == SortMode.DISTANCE) {
        // Already sorted by distance: state the reference point instead of
        // offering an action, so the number on each row is attributable.
        Text(
            text = strings.nearbyReference(resolved.label ?: strings.nearbyYourLocation),
            style = MaterialTheme.typography.labelSmall,
            color = colours.textFaint,
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        )
        return
    }

    val requesting = status is LocationStatus.Requesting
    val action = when {
        requesting -> strings.nearbyLocating
        resolved != null -> strings.nearbySortAction
        else -> strings.nearbyInviteAction
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(VendavalTheme.radii.small))
            .clickable(enabled = !requesting) {
                // Both halves of the intent in one tap. Asking for permission and
                // then leaving the list in its previous order would make the user
                // do the second half themselves, and they already said what they
                // wanted by tapping.
                catalogue.onSortChange(SortMode.DISTANCE)
                if (resolved == null) catalogue.requestLocation()
            }
            .padding(vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.NearMe,
            contentDescription = null,
            tint = if (requesting) colours.textFaint else colours.accent,
            modifier = Modifier.size(16.dp),
        )
        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(
                text = strings.nearbyInvite,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = action,
                style = MaterialTheme.typography.labelSmall,
                color = if (requesting) colours.textFaint else colours.accent,
            )
        }
    }
}

@Composable
private fun Panel(content: @Composable () -> Unit) {
    val colours = VendavalTheme.colors
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(VendavalTheme.radii.medium))
            .background(colours.surfaceSunken)
            .padding(12.dp),
    ) { content() }
}

/**
 * Filter pill.
 *
 * Selected state inverts to solid foreground rather than tinting with the accent:
 * with five type chips visible at once, five accent-tinted pills would compete
 * with each other and with the venue hues.
 */
@Composable
private fun PillChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    dotColour: androidx.compose.ui.graphics.Color? = null,
) {
    val colours = VendavalTheme.colors
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (selected) MaterialTheme.colorScheme.onSurface else androidx.compose.ui.graphics.Color.Transparent)
            .border(
                1.dp,
                if (selected) MaterialTheme.colorScheme.onSurface else colours.line,
                RoundedCornerShape(999.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 13.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (dotColour != null && !selected) {
            Box(Modifier.size(6.dp).clip(CircleShape).background(dotColour))
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) MaterialTheme.colorScheme.surface else colours.textMuted,
        )
    }
}

@Composable
private fun DisclosureChip(
    label: String,
    badge: Int,
    expanded: Boolean,
    onClick: () -> Unit,
) {
    val colours = VendavalTheme.colors
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .border(1.dp, colours.lineStrong, RoundedCornerShape(999.dp))
            .clickable(onClick = onClick)
            .padding(start = 13.dp, end = 9.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = if (badge > 0) "$label · $badge" else label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Icon(
            imageVector = if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
            contentDescription = null,
            tint = colours.textMuted,
            modifier = Modifier.size(16.dp),
        )
    }
}
