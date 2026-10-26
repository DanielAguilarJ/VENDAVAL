package mx.vendaval.app.navigation

import mx.vendaval.app.i18n.stringsFor
import mx.vendaval.core.data.repository.AppLanguage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Invariants of the primary navigation.
 *
 * The bar and the rail are drawn by hand, and the active tab is marked by swapping an
 * outline glyph for a solid one. Nothing in the type system enforces that the two
 * glyphs actually differ, so a destination declared with the same icon twice would
 * compile, run, and simply stop showing which tab is live — the failure is invisible
 * rather than loud. These tests are here to make it loud.
 */
class TopLevelDestinationTest {

    @Test
    fun every_destination_distinguishes_its_active_glyph() {
        TopLevelDestination.entries.forEach { destination ->
            assertNotEquals(
                destination.icon,
                destination.selectedIcon,
                "${destination.name} uses the same glyph for both states, so its " +
                    "active state would be carried by colour alone.",
            )
        }
    }

    @Test
    fun no_two_destinations_share_a_glyph() {
        val outlines = TopLevelDestination.entries.map { it.icon }
        assertEquals(
            outlines.size,
            outlines.distinct().size,
            "Two destinations draw the same outline icon, so the bar would show a " +
                "duplicate and the tabs would be indistinguishable at rest.",
        )
    }

    @Test
    fun every_destination_has_a_label_in_every_locale() {
        // Labels are always visible in this nav — there is no icon-only state to fall
        // back on — so a missing translation would leave a blank gap under an icon.
        AppLanguage.entries.forEach { language ->
            val strings = stringsFor(language)
            TopLevelDestination.entries.forEach { destination ->
                val label = destination.label(strings)
                assertTrue(
                    label.isNotBlank(),
                    "${destination.name} has no label in $language, and the bar " +
                        "always shows labels.",
                )
            }
        }
    }

    @Test
    fun only_the_map_is_full_bleed() {
        // The reading-measure cap in the shell keys off this flag. If a text screen
        // ever opted in it would stretch a single column of cards across a desktop
        // window, which is the layout the cap exists to prevent.
        val fullBleed = TopLevelDestination.entries.filter { it.isFullBleed }
        assertEquals(listOf(TopLevelDestination.MAP), fullBleed)
    }
}
