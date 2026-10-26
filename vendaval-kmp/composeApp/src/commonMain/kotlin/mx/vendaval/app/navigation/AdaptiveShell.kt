package mx.vendaval.app.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.vendaval.app.i18n.LocalStrings
import mx.vendaval.app.theme.VendavalTheme

/** Layout regimes, keyed off available width. */
enum class WindowSize {
    /** Phones. Bottom bar, single pane, detail as a sheet. */
    COMPACT,

    /** Large phones in landscape and small tablets. Rail, single pane. */
    MEDIUM,

    /** Tablets and desktop. Rail plus a persistent detail pane. */
    EXPANDED,
    ;

    val usesRail: Boolean get() = this != COMPACT

    /** Whether a selected venue gets its own pane instead of an overlay. */
    val usesDetailPane: Boolean get() = this == EXPANDED
}

/**
 * Chrome for the whole app.
 *
 * Breakpoints are measured with [BoxWithConstraints] rather than through the
 * `material3-window-size-class` artifact: the app needs three thresholds on one axis,
 * which is a comparison against a `Dp`, and avoiding the dependency keeps desktop and
 * iOS on exactly the same code path as Android.
 *
 * The thresholds follow Material's own guidance — 600 dp and 840 dp — so the layout
 * changes where users of other apps already expect it to.
 */
@Composable
fun AdaptiveShell(
    navigator: AppNavigator,
    modifier: Modifier = Modifier,
    detailPane: (@Composable () -> Unit)? = null,
    content: @Composable (WindowSize, PaddingValues) -> Unit,
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val windowSize = when {
            maxWidth < COMPACT_LIMIT -> WindowSize.COMPACT
            maxWidth < EXPANDED_LIMIT -> WindowSize.MEDIUM
            else -> WindowSize.EXPANDED
        }

        if (windowSize.usesRail) {
            Row(Modifier.fillMaxSize()) {
                DestinationRail(navigator)

                Box(Modifier.weight(if (windowSize.usesDetailPane) 1.35f else 1f)) {
                    // A single-column list stretched across 1280 px leaves every
                    // card two-thirds empty. Capping the measure keeps line length
                    // in the readable 45-75 character band and stops the layout
                    // from looking like a resized phone screen.
                    //
                    // The map is exempt. Boxing it left a dead black column beside
                    // the rail: a map's whole job is to fill the space it is given,
                    // and there is no line length in it to protect.
                    val measure = if (navigator.destination.isFullBleed) {
                        Modifier.fillMaxSize()
                    } else {
                        Modifier
                            .widthIn(max = READING_MEASURE)
                            .fillMaxHeight()
                    }

                    Box(modifier = measure.align(Alignment.TopCenter)) {
                        content(windowSize, PaddingValues(20.dp))
                    }
                }

                // The detail pane is only mounted when there is something to show, so
                // an empty pane never steals width from the list.
                if (windowSize.usesDetailPane && detailPane != null) {
                    Box(
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(end = 12.dp, top = 12.dp, bottom = 12.dp),
                    ) {
                        detailPane()
                    }
                }
            }
        } else {
            Column(Modifier.fillMaxSize()) {
                Box(Modifier.weight(1f)) {
                    content(windowSize, PaddingValues(16.dp))
                }
                DestinationBar(navigator)
            }
        }
    }
}

/**
 * The bottom navigation bar, built by hand.
 *
 * ## Why this is not `NavigationBar`
 * It used to be — stock `NavigationBar` + `NavigationBarItem` with
 * `alwaysShowLabel = false`. That gave Material 3's default behaviour, and the
 * default is the single strongest "generated interface" tell in the whole app:
 *
 * 1. A **filled pill indicator** slides in behind the active icon. Nothing else in
 *    this app uses a capsule-shaped fill, so it reads as framework furniture rather
 *    than as a decision. None of the surveyed references (Qonto, Monzo, Strava,
 *    Hulu, Starbucks, Phantom, Opal, Opera, Comet, Crouton) use one.
 * 2. `alwaysShowLabel = false` shows the label **only** on the active tab, so
 *    every tap re-lays out the row and the icons visibly jump. The references all
 *    keep labels permanently visible, at a size that stays quiet.
 * 3. The default 80 dp height plus Material's own padding left the bar taller than
 *    the content it serves.
 *
 * So: a hairline over a flat bar, five equal columns, labels always present, and
 * the active state carried by **colour and a 2 dp top rule over the live tab** —
 * the pattern the references converge on. No blob.
 */
@Composable
private fun DestinationBar(navigator: AppNavigator) {
    val strings = LocalStrings.current
    val colours = VendavalTheme.colors

    Column(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background)) {
        // The bar earns its separation from a hairline, not from elevation or a
        // tonal shift, so it sits flush with the content instead of floating. It used
        // to sit on `surfaceSunken`, a step lighter than the page — Material's
        // "elevation reads lighter in dark theme" convention, which made the bar a
        // separate slab of chrome. The references keep it flush and let the line do
        // the work, and it matches how the venue rows lost their card backgrounds.
        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(colours.line),
        )
        Row(Modifier.fillMaxWidth().height(BAR_HEIGHT)) {
            TopLevelDestination.entries.forEach { destination ->
                val selected = navigator.destination == destination
                val label = destination.label(strings)
                // Near-white, not the accent, for the live tab. A saturated cyan on
                // every active icon *and* its label made the nav glow — the exact
                // "electric teal on blue-black" signature that reads as generated.
                // The references mark the live tab with contrast and icon weight and
                // spend brand colour sparingly, so the accent survives here only in
                // the 2 dp rule.
                val tint by animateColorAsState(
                    targetValue = if (selected) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        colours.textFaint
                    },
                    // One short tween, no spring or bounce. Motion here exists to
                    // soften a hard state flip, not to draw attention to itself.
                    animationSpec = tween(durationMillis = NAV_MOTION_MS),
                    label = "navTint",
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            // No default indication. Compose's stock ripple painted a
                            // grey block across the whole tab on hover and on press,
                            // which competed with the accent for "which tab is live"
                            // and was itself a Material default artifact. Selection is
                            // already legible from colour plus the rule.
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            role = Role.Tab,
                            onClickLabel = label,
                            onClick = { navigator.select(destination) },
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // Top rule marks the live tab. Its width animates from zero, so
                    // the rule draws outward from the centre of the tab you tapped
                    // rather than blinking into place. Height is fixed, so the row
                    // never reflows — which is what the Material pill failed to
                    // guarantee when it also had to make room for a label.
                    val ruleWidth by animateDpAsState(
                        targetValue = if (selected) INDICATOR_WIDTH else 0.dp,
                        animationSpec = tween(durationMillis = NAV_MOTION_MS),
                        label = "navRule",
                    )
                    Box(
                        Modifier
                            .width(ruleWidth)
                            .height(2.dp)
                            .background(colours.accent),
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            // Solid glyph for the tab you are on, outline for the rest.
                            imageVector = if (selected) {
                                destination.selectedIcon
                            } else {
                                destination.icon
                            },
                            contentDescription = null,
                            tint = tint,
                            modifier = Modifier.size(21.dp),
                        )
                        Text(
                            text = label,
                            // Tracking zeroed for the same reason as the rail: the
                            // 1.6 sp on labelSmall exists for small-caps section
                            // headings and clips a word like "Directorio" here.
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 0.sp,
                            ),
                            color = tint,
                            maxLines = 1,
                            modifier = Modifier.padding(top = 5.dp),
                        )
                    }
                }
            }
        }
    }
}

/**
 * The wide-window rail — the same nav, rebuilt to the same rules as the bar.
 *
 * It was stock `NavigationRail` + `NavigationRailItem`, which carries the identical
 * Material default pill indicator that the bottom bar just lost. Leaving it would
 * have meant the app's primary navigation looked hand-made on a phone and
 * framework-default on a desktop. Same language here: hairline separation, labels
 * always visible, active state carried by colour plus a rule — vertical and on the
 * leading edge, because that is where a rule belongs when the list runs downward.
 */
@Composable
private fun DestinationRail(navigator: AppNavigator) {
    val strings = LocalStrings.current
    val colours = VendavalTheme.colors

    Row(Modifier.fillMaxHeight()) {
        Column(
            modifier = Modifier
                .width(RAIL_WIDTH)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TopLevelDestination.entries.forEach { destination ->
                val selected = navigator.destination == destination
                val label = destination.label(strings)
                // Near-white, not the accent, for the live tab. A saturated cyan on
                // every active icon *and* its label made the nav glow — the exact
                // "electric teal on blue-black" signature that reads as generated.
                // The references mark the live tab with contrast and icon weight and
                // spend brand colour sparingly, so the accent survives here only in
                // the 2 dp rule.
                val tint by animateColorAsState(
                    targetValue = if (selected) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        colours.textFaint
                    },
                    // One short tween, no spring or bounce. Motion here exists to
                    // soften a hard state flip, not to draw attention to itself.
                    animationSpec = tween(durationMillis = NAV_MOTION_MS),
                    label = "navTint",
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(RAIL_ITEM_HEIGHT)
                        .clickable(
                            // Same reason as the bar: no stock ripple block.
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            role = Role.Tab,
                            onClickLabel = label,
                            onClick = { navigator.select(destination) },
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Leading rule, growing from the item's centre. Vertical here, so
                    // it is the height that animates; width stays fixed so the icon
                    // and label never shift sideways.
                    val ruleHeight by animateDpAsState(
                        targetValue = if (selected) INDICATOR_WIDTH else 0.dp,
                        animationSpec = tween(durationMillis = NAV_MOTION_MS),
                        label = "railRule",
                    )
                    Box(
                        Modifier
                            .width(2.dp)
                            .height(ruleHeight)
                            .background(colours.accent),
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            // Solid glyph for the tab you are on, outline for the rest.
                            imageVector = if (selected) {
                                destination.selectedIcon
                            } else {
                                destination.icon
                            },
                            contentDescription = null,
                            tint = tint,
                            modifier = Modifier.size(21.dp),
                        )
                        Text(
                            text = label,
                            // Same zeroed tracking as the bar: labelSmall's 1.6 sp is
                            // for small-caps headings and clipped "Directorio" here.
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 0.sp,
                            ),
                            color = tint,
                            maxLines = 1,
                            modifier = Modifier.padding(top = 5.dp),
                        )
                    }
                }
            }
        }
        // Hairline instead of the rail's default tonal block, so the seam between
        // navigation and content is a line rather than a change of material.
        Box(
            Modifier
                .width(1.dp)
                .fillMaxHeight()
                .background(colours.line),
        )
    }
}

/**
 * Maximum measure for the single-column content area.
 *
 * Chosen so body text stays in the 45-75 character band that reading research
 * treats as comfortable; beyond it the eye loses the line on return sweeps.
 */
private val READING_MEASURE: Dp = 720.dp

private val COMPACT_LIMIT: Dp = 600.dp
private val EXPANDED_LIMIT: Dp = 840.dp
private val RAIL_WIDTH: Dp = 88.dp

/**
 * Bar height. Shorter than Material's 80 dp default: with labels always visible
 * there is no reflow to reserve room for, so the bar only needs to hold one icon
 * and one 10 sp line.
 */
private val BAR_HEIGHT: Dp = 60.dp

/** Width of the active-tab top rule. Narrower than the column, so it reads as a mark. */
private val INDICATOR_WIDTH: Dp = 26.dp

/** Height of one rail entry. Gives the icon and its label room without a pill. */
private val RAIL_ITEM_HEIGHT: Dp = 64.dp

/**
 * Duration of the active-tab transition.
 *
 * Short on purpose. Navigation feedback has to feel immediate — anything long
 * enough to notice as an animation reads as lag when you are just changing tabs.
 * Mobbin returns still screenshots, so this is not derived from the references;
 * it follows the platform convention for a small state change.
 */
private const val NAV_MOTION_MS = 180
