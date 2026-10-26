package mx.vendaval.app.feature.favourites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import mx.vendaval.app.theme.VendavalTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mx.vendaval.app.i18n.LocalStrings
import mx.vendaval.app.presentation.CatalogueStateHolder
import mx.vendaval.app.presentation.LocationStatus
import mx.vendaval.app.ui.component.EmptyState
import mx.vendaval.app.ui.component.SpaceCard
import mx.vendaval.core.domain.geo.distanceKm
import mx.vendaval.core.domain.model.SpaceId
import mx.vendaval.core.domain.model.SpaceListItem

/**
 * Saved venues.
 *
 * Computed from the full catalogue rather than from the filtered directory result:
 * a user who opens "favourites" expects their whole list, not their list narrowed by
 * a search they typed on another tab.
 */
@Composable
fun FavouritesScreen(
    catalogue: CatalogueStateHolder,
    onOpenSpace: (SpaceId) -> Unit,
    modifier: Modifier = Modifier,
    selectedId: SpaceId? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
) {
    val strings = LocalStrings.current
    val spaces by catalogue.allSpaces.collectAsState()
    val favourites by catalogue.favourites.collectAsState()
    val locationStatus by catalogue.locationStatus.collectAsState()

    val items: List<SpaceListItem> = remember(spaces, favourites, locationStatus) {
        val reference = (locationStatus as? LocationStatus.Resolved)?.point
        spaces
            .filter { it.id in favourites }
            .map { space ->
                SpaceListItem(
                    space = space,
                    isFavourite = true,
                    distanceKm = reference?.let { distanceKm(it, space.location) },
                )
            }
            .sortedBy { it.space.name }
    }

    // The empty state keeps the masthead above it. Dropping the heading and showing
    // only an icon leaves the user unsure whether the tab failed to load or is simply
    // empty — the reference saved-item screens always keep the title in place.
    if (items.isEmpty()) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(contentPadding),
        ) {
            Masthead(count = 0)
            EmptyState(
                icon = Icons.Outlined.StarBorder,
                title = strings.favouritesEmptyTitle,
                body = strings.favouritesEmptyBody,
                modifier = Modifier.weight(1f),
            )
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        item(key = "title") {
            Masthead(count = items.size)
        }
        items(items, key = { it.id.value }) { item ->
            SpaceCard(
                item = item,
                onClick = { onOpenSpace(item.id) },
                onToggleFavourite = { catalogue.onToggleFavourite(item.id) },
                isSelected = item.id == selectedId,
            )
        }
    }
}

/** Title plus how many, matching the directory so the two tabs read as siblings. */
@Composable
private fun Masthead(count: Int) {
    val strings = LocalStrings.current
    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
        Text(
            text = strings.navFavourites,
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = strings.resultCount(count),
            style = MaterialTheme.typography.bodyMedium,
            color = VendavalTheme.colors.textFaint,
        )
    }
}
