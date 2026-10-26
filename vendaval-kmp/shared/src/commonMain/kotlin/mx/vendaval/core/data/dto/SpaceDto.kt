package mx.vendaval.core.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import mx.vendaval.core.domain.geo.distanceKm
import mx.vendaval.core.domain.model.CulturalSpace
import mx.vendaval.core.domain.model.GeoPoint
import mx.vendaval.core.domain.model.Municipality
import mx.vendaval.core.domain.model.PhoneNumber
import mx.vendaval.core.domain.model.SocialPlatform
import mx.vendaval.core.domain.model.SpaceId
import mx.vendaval.core.domain.model.SpaceOrigin
import mx.vendaval.core.domain.model.VenueType

/**
 * Wire and cache representation of a venue.
 *
 * Field names mirror the `espacios_culturales` columns exactly (note the
 * camelCase `mapQuery`, which is a quoted identifier in Postgres) so the same
 * type serves PostgREST reads, PostgREST writes and the on-disk cache without a
 * second mapping layer.
 *
 * Every field is nullable with a default: the table is publicly writable, so the
 * decoder must survive missing columns and nulls rather than failing the whole
 * batch because one row is malformed.
 */
@Serializable
data class SpaceDto(
    val id: Long? = null,
    val name: String? = null,
    val address: String? = null,
    val phone: String? = null,
    val type: String? = null,
    @SerialName("municipio") val municipality: String? = null,
    val lat: Double? = null,
    val lng: Double? = null,
    val mapQuery: String? = null,
    /** Local-only bookkeeping; ignored by PostgREST because it is stripped on write. */
    val origin: String? = null,
    /**
     * Community-authored profile fields. Additive and nullable so a stale cache row
     * (or a PostgREST read from a table that lacks these columns) still decodes. They
     * are carried in the on-disk cache but stripped from the insert payload — see
     * [toInsertDto] — because sending a column the table does not have makes PostgREST
     * reject the whole row.
     */
    val description: String? = null,
    val hours: String? = null,
    val website: String? = null,
    val social: Map<String, String>? = null,
)

/**
 * Projects a row onto the domain, or `null` when it cannot be salvaged.
 *
 * Salvage rules, in order of preference, exist because dropping a community
 * contribution over a data-entry slip is worse than accepting an imperfect one:
 *  - an unparseable venue type becomes [VenueType.OTRO];
 *  - an unrecognised municipality is inferred from the coordinates;
 *  - only a missing name or unusable coordinates are fatal.
 */
fun SpaceDto.toDomainOrNull(fallbackOrigin: SpaceOrigin): CulturalSpace? {
    val cleanName = name?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    val point = GeoPoint.orNull(lat, lng) ?: return null

    val resolvedMunicipality = Municipality.fromLabel(municipality)
        ?: nearestMunicipality(point)

    val resolvedId = id?.let(::SpaceId) ?: return null

    return CulturalSpace(
        id = resolvedId,
        name = cleanName,
        address = address?.trim().orEmpty(),
        phone = PhoneNumber.orNull(phone),
        type = VenueType.fromLabel(type),
        municipality = resolvedMunicipality,
        location = point,
        mapQuery = mapQuery?.trim().orEmpty(),
        origin = origin?.let { raw ->
            SpaceOrigin.entries.firstOrNull { it.name.equals(raw, ignoreCase = true) }
        } ?: fallbackOrigin,
        description = description?.trim()?.takeIf { it.isNotEmpty() },
        hours = hours?.trim()?.takeIf { it.isNotEmpty() },
        website = website?.trim()?.takeIf { it.isNotEmpty() },
        socialLinks = social?.mapNotNull { (key, value) ->
            val platform = SocialPlatform.fromKey(key) ?: return@mapNotNull null
            val clean = value.trim().takeIf { it.isNotEmpty() } ?: return@mapNotNull null
            platform to clean
        }?.toMap().orEmpty(),
    )
}

/** Attributes a stray coordinate to the closest municipal seat. */
private fun nearestMunicipality(point: GeoPoint): Municipality =
    Municipality.entries.minBy { distanceKm(point, it.approximateCentre) }

fun CulturalSpace.toDto(): SpaceDto = SpaceDto(
    id = id.value,
    name = name,
    address = address,
    phone = phone?.display,
    type = type.label,
    municipality = municipality.label,
    lat = location.latitude,
    lng = location.longitude,
    mapQuery = mapQuery,
    origin = origin.name,
    description = description,
    hours = hours,
    website = website,
    social = socialLinks.takeIf { it.isNotEmpty() }
        ?.entries?.associate { (platform, value) -> platform.name to value },
)

/**
 * Insert payload. The id is omitted so Postgres assigns one from the sequence, and
 * `origin` is dropped because it is a client-side concept. The community-profile
 * fields ([description], [hours], [website], [social]) are dropped too: the shared
 * `espacios_culturales` table has no columns for them, and sending an unknown column
 * would make PostgREST reject the whole insert. They still round-trip through the
 * local cache via [toDto].
 */
fun CulturalSpace.toInsertDto(): SpaceDto = toDto().copy(
    id = null,
    origin = null,
    description = null,
    hours = null,
    website = null,
    social = null,
)
