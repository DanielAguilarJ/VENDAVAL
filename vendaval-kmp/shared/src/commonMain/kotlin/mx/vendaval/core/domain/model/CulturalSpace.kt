package mx.vendaval.core.domain.model

import mx.vendaval.core.domain.text.foldForSearch
import mx.vendaval.core.domain.text.percentEncode
import kotlin.jvm.JvmInline

/**
 * Identity of a catalogue entry.
 *
 * Server-assigned ids are positive (Postgres `bigserial`). Locally created
 * entries that have not been accepted by the backend yet get negative ids from
 * [SpaceId.nextLocal], which guarantees they can never collide with a server id
 * and makes "is this pending upload?" a cheap check.
 */
@JvmInline
value class SpaceId(val value: Long) {
    val isLocal: Boolean get() = value < 0

    override fun toString(): String = value.toString()

    companion object {
        fun nextLocal(existing: Collection<SpaceId>): SpaceId {
            val lowest = existing.minOfOrNull { it.value } ?: 0L
            return SpaceId(minOf(lowest, 0L) - 1L)
        }
    }
}

/** Where a record came from, which drives the sync badge and delete affordances. */
enum class SpaceOrigin {
    /** Compiled into the binary; always available, never deleted. */
    SEED,

    /** Fetched from Postgres. */
    REMOTE,

    /** Created on this device and not yet acknowledged by the backend. */
    LOCAL,
}

/**
 * A public cultural venue.
 *
 * This is the aggregate the whole app is built around. It is intentionally flat
 * and free of framework annotations — serialization lives in the data layer's
 * DTOs so the wire format can change without touching domain code.
 */
data class CulturalSpace(
    val id: SpaceId,
    val name: String,
    val address: String,
    val phone: PhoneNumber?,
    val type: VenueType,
    val municipality: Municipality,
    val location: GeoPoint,
    val mapQuery: String,
    val origin: SpaceOrigin,
    /**
     * Community-authored profile fields, all optional. Defaulted so the 27 compiled
     * seeds and every existing construction site keep compiling untouched; only
     * records created through the submission form populate them.
     */
    val description: String? = null,
    val hours: String? = null,
    val website: String? = null,
    val socialLinks: Map<SocialPlatform, String> = emptyMap(),
) {
    /**
     * Pre-folded haystack for accent-insensitive search.
     *
     * Lazy so the cost is paid once per record instead of once per keystroke;
     * with a few thousand rows and a filter running on every character this is
     * the difference between a smooth and a stuttering search field.
     */
    val searchIndex: String by lazy(LazyThreadSafetyMode.NONE) {
        listOf(name, address, type.label, municipality.label)
            .joinToString(" ")
            .foldForSearch()
    }

    /** Universal Google Maps link, mirroring the original web behaviour. */
    val googleMapsUrl: String
        get() {
            val query = mapQuery.takeIf { it.isNotBlank() }
                ?.replace('+', ' ')
                ?: "$name, ${municipality.label}, Aguascalientes"
            return "https://www.google.com/maps/search/?api=1&query=${query.percentEncode()}"
        }

    /**
     * `geo:` URI with a label, which lets Android hand off to whichever map app
     * the user actually has installed instead of forcing Google Maps.
     */
    val geoUri: String
        get() = "geo:${location.latitude},${location.longitude}" +
            "?q=${location.latitude},${location.longitude}(${name.percentEncode()})"

    /** Apple Maps deep link for the iOS target. */
    val appleMapsUrl: String
        get() = "https://maps.apple.com/?ll=${location.latitude},${location.longitude}" +
            "&q=${name.percentEncode()}"
}
