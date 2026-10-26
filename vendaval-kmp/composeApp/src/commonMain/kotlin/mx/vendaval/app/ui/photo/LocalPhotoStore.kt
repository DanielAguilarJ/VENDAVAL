package mx.vendaval.app.ui.photo

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * The app's photo loader, reachable from anywhere that draws a venue.
 *
 * A composition local rather than a parameter because the alternative is
 * threading a store through `DirectoryScreen` → `SpaceCard` → `VenueCover`,
 * `MapScreen` → `VenueCover`, and `SpaceDetailContent` → `DetailHero` — five
 * signatures widened so that one leaf can fetch an image. The store has app
 * lifetime and there is exactly one, which is the case composition locals exist
 * for.
 *
 * Defaults to null so a preview or a test can render a venue without standing up
 * an HTTP client; a null store simply means every venue draws its generated
 * cover, which is a state the UI already has to handle for the half of the
 * catalogue that has no photograph.
 */
val LocalPhotoStore: ProvidableCompositionLocal<PhotoStore?> = staticCompositionLocalOf { null }
