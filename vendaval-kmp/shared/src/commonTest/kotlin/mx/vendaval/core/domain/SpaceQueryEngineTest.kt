package mx.vendaval.core.domain

import mx.vendaval.core.data.seed.SeedCatalogue
import mx.vendaval.core.domain.model.GeoPoint
import mx.vendaval.core.domain.model.Municipality
import mx.vendaval.core.domain.model.PhoneNumber
import mx.vendaval.core.domain.model.SortMode
import mx.vendaval.core.domain.model.SpaceFilter
import mx.vendaval.core.domain.model.SpaceId
import mx.vendaval.core.domain.model.VenueType
import mx.vendaval.core.domain.text.foldForSearch
import mx.vendaval.core.domain.usecase.ComputeCoverageStats
import mx.vendaval.core.domain.usecase.SpaceQueryEngine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

private val catalogue = SeedCatalogue.spaces

private fun query(
    filter: SpaceFilter = SpaceFilter(),
    sort: SortMode = SortMode.NAME,
    favourites: Set<SpaceId> = emptySet(),
    reference: GeoPoint? = null,
) = SpaceQueryEngine(catalogue, favourites, filter, sort, reference)

class SpaceQueryEngineTest {

    @Test
    fun an_empty_filter_returns_the_whole_catalogue() {
        assertEquals(catalogue.size, query().size)
    }

    @Test
    fun search_ignores_accents_in_both_directions() {
        // The venue is spelled "Galería"; the user types "galeria".
        val withoutAccent = query(SpaceFilter(query = "galeria"))
        val withAccent = query(SpaceFilter(query = "galería"))

        assertTrue(withoutAccent.isNotEmpty(), "accent-insensitive search found nothing")
        assertEquals(withAccent.size, withoutAccent.size)
    }

    @Test
    fun search_is_case_insensitive_and_matches_the_address() {
        val byStreet = query(SpaceFilter(query = "VENUSTIANO carranza"))
        assertTrue(byStreet.size >= 3, "expected several venues on that street")
    }

    @Test
    fun search_matches_the_municipality_name() {
        val results = query(SpaceFilter(query = "calvillo"))
        assertTrue(results.all { it.space.municipality == Municipality.CALVILLO })
        assertTrue(results.isNotEmpty())
    }

    @Test
    fun an_empty_type_set_means_unconstrained_not_empty() {
        assertEquals(catalogue.size, query(SpaceFilter(types = emptySet())).size)
    }

    @Test
    fun type_filter_narrows_to_the_selected_types_only() {
        val results = query(SpaceFilter(types = setOf(VenueType.TEATRO)))

        assertTrue(results.isNotEmpty())
        assertTrue(results.all { it.space.type == VenueType.TEATRO })
    }

    @Test
    fun multiple_types_are_combined_as_a_union() {
        val selected = setOf(VenueType.TEATRO, VenueType.GALERIA_DE_ARTE)
        val results = query(SpaceFilter(types = selected))

        val expected = catalogue.count { it.type in selected }
        assertEquals(expected, results.size)
    }

    @Test
    fun type_and_municipality_filters_are_combined_as_an_intersection() {
        val results = query(
            SpaceFilter(
                types = setOf(VenueType.CASA_DE_CULTURA),
                municipalities = setOf(Municipality.AGUASCALIENTES),
            ),
        )

        assertTrue(
            results.all {
                it.space.type == VenueType.CASA_DE_CULTURA &&
                    it.space.municipality == Municipality.AGUASCALIENTES
            },
        )
    }

    @Test
    fun favourites_only_returns_exactly_the_starred_records() {
        val starred = setOf(SpaceId(1), SpaceId(9))
        val results = query(SpaceFilter(favouritesOnly = true), favourites = starred)

        assertEquals(2, results.size)
        assertTrue(results.all { it.isFavourite })
    }

    @Test
    fun name_sort_is_alphabetical_ignoring_accents() {
        val names = query(sort = SortMode.NAME).map { it.space.name.foldForSearch() }
        assertEquals(names.sorted(), names)
    }

    @Test
    fun distance_sort_orders_nearest_first_and_populates_the_distance() {
        val reference = GeoPoint(21.8804, -102.2965)
        val results = query(sort = SortMode.DISTANCE, reference = reference)

        val distances = results.map { requireNotNull(it.distanceKm) }
        assertEquals(distances.sorted(), distances)
    }

    @Test
    fun distance_sort_falls_back_to_name_when_there_is_no_reference() {
        val withoutReference = query(sort = SortMode.DISTANCE, reference = null)
        val byName = query(sort = SortMode.NAME)

        assertEquals(byName.map { it.id }, withoutReference.map { it.id })
        assertTrue(withoutReference.all { it.distanceKm == null })
    }

    @Test
    fun a_query_matching_nothing_returns_an_empty_list() {
        assertTrue(query(SpaceFilter(query = "zzzz-no-such-venue")).isEmpty())
    }
}

class CoverageStatsTest {

    @Test
    fun totals_are_derived_from_the_catalogue_not_hard_coded() {
        val stats = ComputeCoverageStats(catalogue, emptySet())

        assertEquals(catalogue.size, stats.totalSpaces)
        assertEquals(Municipality.entries.size, stats.municipalitiesInState)
    }

    @Test
    fun the_seeded_catalogue_covers_seven_of_eleven_municipalities() {
        val stats = ComputeCoverageStats(catalogue, emptySet())
        assertEquals(7, stats.municipalitiesCovered)
    }

    @Test
    fun the_seeded_catalogue_uses_five_venue_types() {
        val stats = ComputeCoverageStats(catalogue, emptySet())
        assertEquals(5, stats.distinctTypes)
    }

    @Test
    fun type_buckets_sum_to_the_catalogue_size() {
        val stats = ComputeCoverageStats(catalogue, emptySet())
        assertEquals(catalogue.size, stats.byType.values.sum())
    }

    @Test
    fun empty_input_yields_the_empty_stats_rather_than_dividing_by_zero() {
        val stats = ComputeCoverageStats(emptyList(), emptySet())

        assertEquals(0, stats.totalSpaces)
        assertEquals(0f, stats.phoneCoverageRatio)
    }
}

class TolerantParsingTest {

    @Test
    fun venue_types_parse_from_their_exact_wire_labels() {
        assertEquals(VenueType.CASA_DE_CULTURA, VenueType.fromLabel("Casa de Cultura"))
        assertEquals(VenueType.GALERIA_DE_ARTE, VenueType.fromLabel("Galería de Arte"))
    }

    @Test
    fun venue_types_survive_missing_accents_and_odd_casing() {
        assertEquals(VenueType.GALERIA_DE_ARTE, VenueType.fromLabel("galeria de arte"))
        assertEquals(VenueType.TEATRO, VenueType.fromLabel("TEATRO"))
    }

    @Test
    fun an_unknown_venue_type_becomes_otro_instead_of_dropping_the_row() {
        assertEquals(VenueType.OTRO, VenueType.fromLabel("Planetario"))
        assertEquals(VenueType.OTRO, VenueType.fromLabel(null))
    }

    @Test
    fun a_near_miss_venue_type_is_salvaged_by_keyword() {
        assertEquals(VenueType.CASA_DE_CULTURA, VenueType.fromLabel("Casa de la Cultura"))
        assertEquals(VenueType.CENTRO_CULTURAL, VenueType.fromLabel("Biblioteca municipal"))
    }

    @Test
    fun municipalities_parse_with_and_without_accents() {
        assertEquals(Municipality.JESUS_MARIA, Municipality.fromLabel("Jesús María"))
        assertEquals(Municipality.JESUS_MARIA, Municipality.fromLabel("jesus maria"))
        assertEquals(Municipality.SAN_JOSE_DE_GRACIA, Municipality.fromLabel("San José de Gracia"))
    }

    @Test
    fun an_unknown_municipality_is_null_so_the_caller_can_decide() {
        assertNull(Municipality.fromLabel("Zacatecas"))
        assertNull(Municipality.fromLabel(""))
    }

    @Test
    fun the_no_phone_sentinel_becomes_a_real_null() {
        assertNull(PhoneNumber.orNull("No disponible"))
        assertNull(PhoneNumber.orNull("  "))
        assertNull(PhoneNumber.orNull(null))
        assertNull(PhoneNumber.orNull("123"))
    }

    @Test
    fun a_valid_phone_keeps_its_display_form_and_gains_an_e164_uri() {
        val phone = PhoneNumber.orNull("449 910 2010")
        requireNotNull(phone)

        assertEquals("449 910 2010", phone.display)
        assertEquals("4499102010", phone.dialDigits)
        assertEquals("tel:+524499102010", phone.dialUri)
        assertNull(phone.extension)
    }

    @Test
    fun an_extension_is_split_off_so_it_never_reaches_the_dialler() {
        val phone = PhoneNumber.orNull("449 910 2121 ext. 4255")
        requireNotNull(phone)

        assertEquals("4499102121", phone.dialDigits)
        assertEquals("4255", phone.extension)
    }
}

class TextNormalizationTest {

    @Test
    fun folding_lowercases_and_strips_diacritics() {
        assertEquals("galeria de arte", "Galería de Arte".foldForSearch())
        assertEquals("jesus maria", "Jesús María".foldForSearch())
        assertEquals("canadas", "Cañadas".foldForSearch())
    }

    @Test
    fun folding_collapses_whitespace_runs() {
        assertEquals("casa teran", "  Casa    Terán  ".foldForSearch())
    }

    @Test
    fun folding_preserves_meaningful_punctuation() {
        assertEquals("artefacto / espacio experimental", "Artefacto / Espacio Experimental".foldForSearch())
    }
}
