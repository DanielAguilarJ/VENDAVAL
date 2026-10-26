package mx.vendaval.core.domain.model

/**
 * A photograph of a real venue, with the credit required to show it.
 *
 * Every field but the path exists because of the licence. These images are other
 * people's work under CC BY-SA, which permits redistribution only with
 * attribution — so [artist] and [licence] are not metadata we might display,
 * they are the condition for displaying the image at all. A `VenuePhoto` that
 * reaches the UI without its credit visible is a licence violation, which is why
 * the credit travels with the image in one object instead of being looked up
 * separately and forgotten.
 *
 * [assetPath] points inside the binary, not at the internet. Fetching these at
 * display time was tried and abandoned: the app is offline-first, so a network
 * dependency fails precisely the users it exists for, and Wikimedia answers a
 * burst of requests with an HTTP 429 whose HTML body decodes to null — producing
 * a missing photo indistinguishable from a venue that has none.
 *
 * Coverage is partial and always will be: roughly half the catalogue has no
 * freely-licensed photograph anywhere. Absence is the normal case, not an error
 * state — a space without one keeps its generated letter cover. Nothing here is
 * ever synthesised or approximated: a photo is either verifiably of that place
 * or it does not exist.
 */
data class VenuePhoto(
    val spaceId: SpaceId,
    /** Compose resource path, e.g. `files/venues/9.jpg`. */
    val assetPath: String,
    val width: Int,
    val height: Int,
    val licence: String,
    val artist: String,
    /** The Commons page, so the credit can link back to the original. */
    val sourceUrl: String,
) {
    /** Single-line attribution, e.g. `Nadia Cisneros · CC BY-SA 4.0`. */
    val credit: String get() = "$artist · $licence"

    /** Width divided by height, for reserving space before the bytes arrive. */
    val aspectRatio: Float
        get() = if (height > 0) width.toFloat() / height.toFloat() else 1f
}
