package mx.vendaval.core.data

import mx.vendaval.core.data.seed.SeedCatalogue
import mx.vendaval.core.data.seed.VenuePhotoSeed
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * `VenuePhotoSeed.kt` is machine-generated, so nobody reviews it line by line on
 * a regenerate. These are the properties that must hold for the app to be
 * allowed to show those images at all, and each one fails silently otherwise:
 * a missing credit is a licence violation that looks fine on screen, and a
 * photo keyed to a space that no longer exists is simply never displayed.
 */
class VenuePhotoSeedTest {

    @Test
    fun every_photo_points_at_a_space_that_exists() {
        val knownIds = SeedCatalogue.spaces.map { it.id }.toSet()
        val orphans = VenuePhotoSeed.photos.keys.filterNot { it in knownIds }
        assertTrue(
            orphans.isEmpty(),
            "photos keyed to unknown spaces: ${orphans.map { it.value }}",
        )
    }

    @Test
    fun no_space_is_listed_twice() {
        // `associateBy` silently keeps the last duplicate, so a double entry
        // would drop a photo without any error.
        val declared = VenuePhotoSeed.photos.size
        val distinct = VenuePhotoSeed.photos.keys.size
        assertEquals(distinct, declared, "duplicate spaceId in the generated seed")
    }

    @Test
    fun every_photo_carries_the_attribution_its_licence_requires() {
        VenuePhotoSeed.photos.values.forEach { photo ->
            assertTrue(photo.artist.isNotBlank(), "no artist for space ${photo.spaceId.value}")
            assertTrue(photo.licence.isNotBlank(), "no licence for space ${photo.spaceId.value}")
            assertTrue(
                photo.sourceUrl.startsWith("https://"),
                "no source page for space ${photo.spaceId.value}",
            )
            assertTrue(
                photo.credit.contains(photo.artist) && photo.credit.contains(photo.licence),
                "credit line drops part of the attribution: ${photo.credit}",
            )
        }
    }

    @Test
    fun every_licence_is_redistributable() {
        // Anything outside this set cannot ship: no "fair use", no unknown, no
        // all-rights-reserved. The ingestor filters for it; this makes a
        // regression a build failure instead of a legal problem.
        val allowed = Regex("""^(CC BY(-SA)?[ -]?\d(\.\d)?|CC0|Public domain|PD.*)$""")
        VenuePhotoSeed.photos.values.forEach { photo ->
            assertTrue(
                allowed.matches(photo.licence),
                "licence '${photo.licence}' is not redistributable (space ${photo.spaceId.value})",
            )
        }
    }

    @Test
    fun every_asset_path_is_a_bundled_resource() {
        // Not a URL: these ship in the binary. A path that drifts out of
        // `composeResources/files/venues/` silently becomes "no photograph", so the
        // shape is pinned here where a regenerate has to notice.
        VenuePhotoSeed.photos.values.forEach { photo ->
            assertTrue(
                photo.assetPath == "files/venues/${photo.spaceId.value}.jpg",
                "unexpected asset path '${photo.assetPath}' for space ${photo.spaceId.value}",
            )
        }
    }

    @Test
    fun declared_dimensions_yield_a_usable_aspect_ratio() {
        // The UI reserves layout space from this before any bytes arrive; a zero
        // or absurd ratio collapses the card instead of showing a placeholder.
        VenuePhotoSeed.photos.values.forEach { photo ->
            assertTrue(photo.width > 0 && photo.height > 0, "bad size for ${photo.spaceId.value}")
            assertTrue(
                photo.aspectRatio > 0.2f && photo.aspectRatio < 5f,
                "implausible aspect ratio ${photo.aspectRatio} for space ${photo.spaceId.value}",
            )
        }
    }
}
