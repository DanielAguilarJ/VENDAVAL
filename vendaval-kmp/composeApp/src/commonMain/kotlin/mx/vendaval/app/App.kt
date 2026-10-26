package mx.vendaval.app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import mx.vendaval.app.feature.add.AddSpaceScreen
import mx.vendaval.app.feature.add.AddSpaceStateHolder
import mx.vendaval.app.feature.coverage.CoverageScreen
import mx.vendaval.app.feature.detail.SpaceDetailContent
import mx.vendaval.app.feature.directory.DirectoryScreen
import mx.vendaval.app.feature.favourites.FavouritesScreen
import mx.vendaval.app.feature.map.MapScreen
import mx.vendaval.app.feature.settings.SettingsScreen
import mx.vendaval.app.i18n.LocalStrings
import mx.vendaval.app.i18n.stringsFor
import mx.vendaval.app.navigation.AdaptiveShell
import mx.vendaval.app.navigation.AppNavigator
import mx.vendaval.app.navigation.Overlay
import mx.vendaval.app.navigation.TopLevelDestination
import mx.vendaval.app.presentation.CatalogueStateHolder
import mx.vendaval.app.presentation.LocationStatus
import mx.vendaval.app.presentation.PreferencesStateHolder
import mx.vendaval.app.presentation.rememberStateHolder
import mx.vendaval.app.theme.VendavalTheme
import mx.vendaval.app.ui.component.AmbientGlow
import mx.vendaval.app.ui.component.GlassCard
import mx.vendaval.app.ui.map.TileStore
import mx.vendaval.app.ui.photo.LocalPhotoStore
import mx.vendaval.app.ui.photo.PhotoStore
import mx.vendaval.core.data.repository.FavouritesRepositoryImpl
import mx.vendaval.core.data.repository.PreferencesRepository
import mx.vendaval.core.data.repository.ThemeMode
import mx.vendaval.core.di.TileClientQualifier
import mx.vendaval.core.domain.geo.distanceKm
import mx.vendaval.core.domain.model.SpaceId
import mx.vendaval.core.domain.model.SpaceListItem
import mx.vendaval.core.domain.repository.SpaceRepository
import mx.vendaval.core.domain.service.ExternalLauncher
import mx.vendaval.core.domain.service.LocationProvider
import org.koin.compose.koinInject

/**
 * Root of the application.
 *
 * App-scoped holders are created here and passed down explicitly rather than being
 * resolved inside each screen. The lifetime of every piece of shared state is
 * therefore visible in one place, and no screen can accidentally construct a second
 * copy of the catalogue.
 */
@Composable
fun App() {
    val spaceRepository = koinInject<SpaceRepository>()
    val favouritesRepository = koinInject<FavouritesRepositoryImpl>()
    val preferencesRepository = koinInject<PreferencesRepository>()
    val locationProvider = koinInject<LocationProvider>()
    val launcher = koinInject<ExternalLauncher>()
    val tileClient = koinInject<io.ktor.client.HttpClient>(qualifier = TileClientQualifier)

    val catalogue = rememberStateHolder {
        CatalogueStateHolder(spaceRepository, favouritesRepository, locationProvider)
    }
    val preferencesHolder = rememberStateHolder {
        PreferencesStateHolder(preferencesRepository)
    }
    val navigator = remember { AppNavigator() }

    val preferences by preferencesHolder.preferences.collectAsState()

    // Tiles are cached for the lifetime of the app, not of the map screen: leaving the
    // map and coming back should not re-download the same viewport.
    val tileScope = rememberCoroutineScope()
    val tileStore = remember(tileClient) {
        TileStore(client = tileClient, scope = tileScope)
    }

    // App-lifetime like the tile store, so scrolling away from the directory and
    // back does not re-decode the same images. No HTTP client: the photographs ship
    // in the binary (see PhotoStore for why that is not merely an optimisation).
    val photoStore = remember { PhotoStore(scope = tileScope) }

    val isDark = when (preferences.themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        // No `isSystemInDarkTheme` exists in common code across all three targets, so
        // "system" resolves to the design's native dark palette.
        ThemeMode.SYSTEM -> true
    }

    VendavalTheme(darkTheme = isDark) {
        CompositionLocalProvider(
            LocalStrings provides stringsFor(preferences.language),
            LocalPhotoStore provides photoStore,
        ) {
            Surface(color = MaterialTheme.colorScheme.background) {
                Box(Modifier.fillMaxSize()) {
                    AmbientGlow()

                    AdaptiveShell(
                        navigator = navigator,
                        modifier = Modifier.safeDrawingPadding(),
                        detailPane = navigator.selectedSpaceId?.let { id ->
                            {
                                DetailPane(
                                    catalogue = catalogue,
                                    navigator = navigator,
                                    launcher = launcher,
                                    id = id,
                                )
                            }
                        },
                    ) { windowSize, contentPadding ->
                        when (navigator.destination) {
                            TopLevelDestination.DIRECTORY -> DirectoryScreen(
                                catalogue = catalogue,
                                selectedId = navigator.selectedSpaceId,
                                onOpenSpace = { id ->
                                    navigator.openDetail(id, asOverlay = !windowSize.usesDetailPane)
                                },
                                onAddSpace = navigator::openAddSpace,
                                onOpenMap = { navigator.select(TopLevelDestination.MAP) },
                                contentPadding = contentPadding,
                            )

                            TopLevelDestination.MAP -> MapScreen(
                                catalogue = catalogue,
                                tileStore = tileStore,
                                selectedId = navigator.selectedSpaceId,
                                onSelect = { id ->
                                    if (id == null) navigator.closeDetail()
                                    else navigator.openDetail(id, asOverlay = false)
                                },
                                onOpenDetail = { id ->
                                    navigator.openDetail(id, asOverlay = !windowSize.usesDetailPane)
                                },
                            )

                            TopLevelDestination.FAVOURITES -> FavouritesScreen(
                                catalogue = catalogue,
                                selectedId = navigator.selectedSpaceId,
                                onOpenSpace = { id ->
                                    navigator.openDetail(id, asOverlay = !windowSize.usesDetailPane)
                                },
                                contentPadding = contentPadding,
                            )

                            TopLevelDestination.COVERAGE -> CoverageScreen(
                                catalogue = catalogue,
                                contentPadding = contentPadding,
                            )

                            TopLevelDestination.SETTINGS -> SettingsScreen(
                                catalogue = catalogue,
                                preferences = preferencesHolder,
                                contentPadding = contentPadding,
                            )
                        }
                    }

                    Overlays(
                        navigator = navigator,
                        catalogue = catalogue,
                        launcher = launcher,
                        spaceRepository = spaceRepository,
                        locationProvider = locationProvider,
                    )

                    LiveInsertBanner(catalogue)
                }
            }
        }
    }
}

/** Detail rendered as a side pane on wide windows. */
@Composable
private fun DetailPane(
    catalogue: CatalogueStateHolder,
    navigator: AppNavigator,
    launcher: ExternalLauncher,
    id: SpaceId,
) {
    val item = rememberListItem(catalogue, id) ?: return
    GlassCard(modifier = Modifier.fillMaxSize()) {
        SpaceDetailContent(
            item = item,
            launcher = launcher,
            onToggleFavourite = { catalogue.onToggleFavourite(id) },
            onShowOnMap = { navigator.showOnMap(id) },
            onDelete = {
                catalogue.onDeleteLocal(id)
                navigator.closeDetail()
            },
            onClose = navigator::closeDetail,
        )
    }
}

/**
 * Projects one catalogue record onto a list item, reactively.
 *
 * Reads the three flows the projection depends on so the detail view updates when the
 * venue is starred, when a location arrives, or when a pending record is published and
 * its origin changes. Reading `StateFlow.value` directly here would render once and
 * then go stale.
 */
@Composable
private fun rememberListItem(catalogue: CatalogueStateHolder, id: SpaceId): SpaceListItem? {
    val spaces by catalogue.allSpaces.collectAsState()
    val favourites by catalogue.favourites.collectAsState()
    val locationStatus by catalogue.locationStatus.collectAsState()

    return remember(spaces, favourites, locationStatus, id) {
        val space = spaces.firstOrNull { it.id == id } ?: return@remember null
        SpaceListItem(
            space = space,
            isFavourite = id in favourites,
            distanceKm = (locationStatus as? LocationStatus.Resolved)
                ?.point
                ?.let { distanceKm(it, space.location) },
        )
    }
}

/**
 * Modal layers.
 *
 * Implemented as an animated overlay rather than `ModalBottomSheet` so the behaviour,
 * the scrim and the dismissal semantics are identical on a phone, a desktop window and
 * an iPad — the platform sheet components differ in all three.
 */
@Composable
private fun Overlays(
    navigator: AppNavigator,
    catalogue: CatalogueStateHolder,
    launcher: ExternalLauncher,
    spaceRepository: SpaceRepository,
    locationProvider: LocationProvider,
) {
    val overlay = navigator.overlay

    AnimatedVisibility(visible = overlay != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.55f))
                // Tapping the scrim dismisses. `indication = null` keeps a ripple from
                // firing across the whole screen.
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = navigator::closeOverlay,
                ),
        )
    }

    AnimatedVisibility(
        visible = overlay != null,
        enter = slideInVertically { it / 2 },
        exit = slideOutVertically { it },
    ) {
        Box(
            modifier = Modifier.fillMaxSize().safeDrawingPadding(),
            contentAlignment = Alignment.BottomCenter,
        ) {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.88f)
                    .padding(horizontal = 8.dp),
                cornerRadius = 24.dp,
                highlighted = true,
            ) {
                when (overlay) {
                    is Overlay.Detail -> {
                        val item = rememberListItem(catalogue, overlay.id)
                        if (item != null) {
                            SpaceDetailContent(
                                item = item,
                                launcher = launcher,
                                onToggleFavourite = { catalogue.onToggleFavourite(overlay.id) },
                                onShowOnMap = { navigator.showOnMap(overlay.id) },
                                onDelete = {
                                    catalogue.onDeleteLocal(overlay.id)
                                    navigator.closeOverlay()
                                },
                                onClose = navigator::closeOverlay,
                            )
                        }
                    }

                    Overlay.AddSpace -> {
                        val holder = rememberStateHolder {
                            AddSpaceStateHolder(spaceRepository, locationProvider)
                        }
                        AddSpaceScreen(holder = holder, onDismiss = navigator::closeOverlay)
                    }

                    null -> Unit
                }
            }
        }
    }
}

/** Transient banner announcing a venue inserted by another client. */
@Composable
private fun LiveInsertBanner(catalogue: CatalogueStateHolder) {
    val strings = LocalStrings.current
    val insert by catalogue.liveInsert.collectAsState()

    LaunchedEffect(insert) {
        if (insert != null) {
            delay(4_000)
            catalogue.dismissLiveInsert()
        }
    }

    Box(Modifier.fillMaxSize().safeDrawingPadding(), contentAlignment = Alignment.TopCenter) {
        AnimatedVisibility(
            visible = insert != null,
            enter = slideInVertically { -it },
            exit = slideOutVertically { -it },
        ) {
            insert?.let { space ->
                GlassCard(modifier = Modifier.padding(12.dp), highlighted = true) {
                    androidx.compose.material3.Text(
                        text = strings.syncNewSpace(space.name),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    )
                }
            }
        }
    }
}
