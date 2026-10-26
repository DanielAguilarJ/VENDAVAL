package mx.vendaval.core.domain

import mx.vendaval.core.domain.model.DraftField
import mx.vendaval.core.domain.model.Municipality
import mx.vendaval.core.domain.model.SocialPlatform
import mx.vendaval.core.domain.model.SpaceDraft
import mx.vendaval.core.domain.model.VenueType
import mx.vendaval.core.domain.validation.SpaceDraftValidator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

private fun validDraft() = SpaceDraft(
    name = "Casa de Cultura El Pinar",
    address = "Av. de la Convención 1914 Norte",
    phone = "449 123 4567",
    type = VenueType.CASA_DE_CULTURA,
    municipality = Municipality.AGUASCALIENTES,
    latitude = "21.8820",
    longitude = "-102.2980",
)

class SpaceDraftValidatorTest {

    @Test
    fun a_complete_draft_passes() {
        val result = SpaceDraftValidator(validDraft())

        assertTrue(result.isValid, "unexpected errors: ${result.errors}")
        assertTrue(result.warnings.isEmpty(), "unexpected warnings: ${result.warnings}")
    }

    @Test
    fun an_empty_draft_reports_every_required_field_at_once() {
        val result = SpaceDraftValidator(SpaceDraft())

        assertFalse(result.isValid)
        listOf(
            DraftField.NAME,
            DraftField.ADDRESS,
            DraftField.TYPE,
            DraftField.MUNICIPALITY,
            DraftField.LATITUDE,
            DraftField.LONGITUDE,
        ).forEach { field ->
            assertTrue(field in result.errors, "$field was not flagged")
        }
    }

    @Test
    fun a_too_short_name_is_rejected() {
        val result = SpaceDraftValidator(validDraft().copy(name = "AB"))
        assertTrue(DraftField.NAME in result.errors)
    }

    @Test
    fun markup_in_free_text_is_rejected() {
        val result = SpaceDraftValidator(
            validDraft().copy(name = "<script>alert(1)</script>"),
        )
        assertTrue(DraftField.NAME in result.errors)
    }

    @Test
    fun an_over_long_address_is_rejected() {
        val result = SpaceDraftValidator(validDraft().copy(address = "x".repeat(400)))
        assertTrue(DraftField.ADDRESS in result.errors)
    }

    @Test
    fun a_blank_phone_is_accepted_because_the_field_is_optional() {
        val result = SpaceDraftValidator(validDraft().copy(phone = ""))
        assertTrue(result.isValid)
        assertNull(result.errors[DraftField.PHONE])
    }

    @Test
    fun a_phone_with_too_few_digits_is_rejected() {
        val result = SpaceDraftValidator(validDraft().copy(phone = "4491"))
        assertTrue(DraftField.PHONE in result.errors)
    }

    @Test
    fun non_numeric_coordinates_are_rejected() {
        val result = SpaceDraftValidator(validDraft().copy(latitude = "north"))
        assertTrue(DraftField.LATITUDE in result.errors)
    }

    @Test
    fun coordinates_outside_the_valid_range_are_rejected() {
        val result = SpaceDraftValidator(validDraft().copy(latitude = "95.0"))
        assertTrue(DraftField.LATITUDE in result.errors)
    }

    @Test
    fun swapped_latitude_and_longitude_are_rejected_with_a_hint() {
        // The classic paste error. Swapping real Aguascalientes coordinates pushes the
        // latitude to -102.29, which is not a latitude at all, so this must block
        // submission — but the message should name the actual mistake.
        val result = SpaceDraftValidator(
            validDraft().copy(latitude = "-102.2980", longitude = "21.8820"),
        )

        assertFalse(result.isValid)
        assertEquals(SpaceDraftValidator.SWAPPED_MESSAGE, result.errors[DraftField.LATITUDE])
    }

    @Test
    fun a_plausible_but_suspicious_pair_only_warns() {
        // Both values are individually legal, but a negative latitude with a positive
        // longitude cannot be anywhere near Aguascalientes. Worth flagging, not blocking.
        val result = SpaceDraftValidator(
            validDraft().copy(latitude = "-21.8820", longitude = "102.2980"),
        )

        assertTrue(result.isValid, "a legal-but-odd pair should still submit")
        assertEquals(SpaceDraftValidator.SWAPPED_MESSAGE, result.warnings[DraftField.LATITUDE])
    }

    @Test
    fun coordinates_outside_the_state_warn_but_still_submit() {
        val result = SpaceDraftValidator(
            validDraft().copy(latitude = "19.4326", longitude = "-99.1332"),
        )

        assertTrue(result.isValid)
        assertNotNull(result.warnings[DraftField.LATITUDE])
    }

    @Test
    fun the_map_query_is_derived_when_the_user_leaves_it_blank() {
        val derived = SpaceDraftValidator.deriveMapQuery(validDraft())

        assertEquals("Casa+de+Cultura+El+Pinar+Aguascalientes+Aguascalientes", derived)
    }

    @Test
    fun an_explicit_map_query_is_preserved_verbatim() {
        val draft = validDraft().copy(mapQuery = "Casa+Cultura+Pinar")
        assertEquals("Casa+Cultura+Pinar", SpaceDraftValidator.deriveMapQuery(draft))
    }

    @Test
    fun the_parsed_coordinate_is_exposed_for_valid_input_only() {
        assertNotNull(SpaceDraftValidator.coordinate(validDraft()))
        assertNull(SpaceDraftValidator.coordinate(validDraft().copy(latitude = "")))
    }

    // --- Optional community-profile fields (added in the self-registration work) ---

    @Test
    fun the_new_optional_fields_are_valid_when_blank() {
        val result = SpaceDraftValidator(validDraft())
        assertTrue(result.isValid, "unexpected errors: ${result.errors}")
        listOf(DraftField.DESCRIPTION, DraftField.HOURS, DraftField.WEBSITE, DraftField.SOCIAL)
            .forEach { assertNull(result.errors[it], "$it should not be flagged when blank") }
    }

    @Test
    fun a_filled_profile_with_valid_fields_passes() {
        val result = SpaceDraftValidator(
            validDraft().copy(
                description = "Centro cultural comunitario con talleres.",
                hours = "Lun a Vie 9:00–18:00",
                website = "https://vendaval.mx",
                socialLinks = mapOf(
                    SocialPlatform.INSTAGRAM to "https://instagram.com/vendaval",
                    SocialPlatform.WHATSAPP to "4491234567",
                ),
            ),
        )
        assertTrue(result.isValid, "unexpected errors: ${result.errors}")
    }

    @Test
    fun an_over_long_description_is_rejected() {
        val result = SpaceDraftValidator(validDraft().copy(description = "x".repeat(601)))
        assertTrue(DraftField.DESCRIPTION in result.errors)
    }

    @Test
    fun markup_in_description_is_rejected() {
        val result = SpaceDraftValidator(validDraft().copy(description = "hola <b>mundo</b>"))
        assertTrue(DraftField.DESCRIPTION in result.errors)
    }

    @Test
    fun an_over_long_hours_string_is_rejected() {
        val result = SpaceDraftValidator(validDraft().copy(hours = "9".repeat(200)))
        assertTrue(DraftField.HOURS in result.errors)
    }

    @Test
    fun a_bare_host_website_is_accepted() {
        val result = SpaceDraftValidator(validDraft().copy(website = "vendaval.mx"))
        assertTrue(result.isValid, "unexpected errors: ${result.errors}")
        assertNull(result.errors[DraftField.WEBSITE])
    }

    @Test
    fun a_non_url_website_is_rejected() {
        val result = SpaceDraftValidator(validDraft().copy(website = "no es una url"))
        assertTrue(DraftField.WEBSITE in result.errors)
    }

    @Test
    fun a_social_handle_with_spaces_is_rejected_and_names_the_platform() {
        val result = SpaceDraftValidator(
            validDraft().copy(socialLinks = mapOf(SocialPlatform.FACEBOOK to "mi pagina")),
        )
        assertTrue(DraftField.SOCIAL in result.errors)
        assertTrue(result.errors.getValue(DraftField.SOCIAL).startsWith("Facebook:"))
    }

    @Test
    fun a_blank_social_handle_is_ignored() {
        val result = SpaceDraftValidator(
            validDraft().copy(socialLinks = mapOf(SocialPlatform.TIKTOK to "   ")),
        )
        assertTrue(result.isValid)
        assertNull(result.errors[DraftField.SOCIAL])
    }
}
