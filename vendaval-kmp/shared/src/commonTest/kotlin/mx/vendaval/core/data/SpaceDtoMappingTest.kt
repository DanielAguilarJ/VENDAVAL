package mx.vendaval.core.data

import mx.vendaval.core.data.dto.SpaceDto
import mx.vendaval.core.data.dto.toDomainOrNull
import mx.vendaval.core.data.dto.toDto
import mx.vendaval.core.data.dto.toInsertDto
import mx.vendaval.core.domain.model.SocialPlatform
import mx.vendaval.core.domain.model.SpaceOrigin
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Locks in the mapping of the community-profile fields added for self-registration:
 * they must round-trip through the local cache but never ride into the PostgREST
 * insert payload (the shared table has no columns for them).
 */
class SpaceDtoMappingTest {

    private fun sampleDto() = SpaceDto(
        id = 7L,
        name = "Casa de Cultura El Pinar",
        address = "Av. de la Convención 1914 Norte",
        phone = "449 123 4567",
        type = "Casa de cultura",
        municipality = "Aguascalientes",
        lat = 21.8820,
        lng = -102.2980,
        mapQuery = "Casa+Cultura+Pinar",
        origin = "LOCAL",
        description = "Centro cultural comunitario con talleres.",
        hours = "Lun a Vie 9:00-18:00",
        website = "https://vendaval.mx",
        // INSTAGRAM is known; the bogus key must be dropped by the decoder.
        social = mapOf("INSTAGRAM" to "https://instagram.com/vendaval", "MYSPACE" to "x"),
    )

    @Test
    fun the_decoder_reads_the_new_fields_and_drops_unknown_platforms() {
        val domain = requireNotNull(sampleDto().toDomainOrNull(SpaceOrigin.LOCAL))

        assertEquals("Centro cultural comunitario con talleres.", domain.description)
        assertEquals("Lun a Vie 9:00-18:00", domain.hours)
        assertEquals("https://vendaval.mx", domain.website)
        assertEquals(
            mapOf(SocialPlatform.INSTAGRAM to "https://instagram.com/vendaval"),
            domain.socialLinks,
        )
    }

    @Test
    fun the_new_fields_survive_a_full_cache_round_trip() {
        val domain = requireNotNull(sampleDto().toDomainOrNull(SpaceOrigin.LOCAL))
        val encoded = domain.toDto()

        assertEquals("Centro cultural comunitario con talleres.", encoded.description)
        assertEquals("Lun a Vie 9:00-18:00", encoded.hours)
        assertEquals("https://vendaval.mx", encoded.website)
        assertEquals(mapOf("INSTAGRAM" to "https://instagram.com/vendaval"), encoded.social)

        val reparsed = requireNotNull(encoded.toDomainOrNull(SpaceOrigin.LOCAL))
        assertEquals(domain.socialLinks, reparsed.socialLinks)
        assertEquals(domain.description, reparsed.description)
    }

    @Test
    fun an_empty_social_map_is_encoded_as_null_not_an_empty_object() {
        val domain = requireNotNull(
            sampleDto().copy(social = null).toDomainOrNull(SpaceOrigin.LOCAL),
        )
        assertTrue(domain.socialLinks.isEmpty())
        assertNull(domain.toDto().social)
    }

    @Test
    fun the_insert_payload_strips_every_community_profile_field() {
        val domain = requireNotNull(sampleDto().toDomainOrNull(SpaceOrigin.LOCAL))
        val insert = domain.toInsertDto()

        // Stripped: these have no column in espacios_culturales.
        assertNull(insert.id)
        assertNull(insert.origin)
        assertNull(insert.description)
        assertNull(insert.hours)
        assertNull(insert.website)
        assertNull(insert.social)

        // Core columns still travel.
        assertEquals("Casa de Cultura El Pinar", insert.name)
        assertEquals(21.8820, insert.lat)
        assertEquals(-102.2980, insert.lng)
    }
}
