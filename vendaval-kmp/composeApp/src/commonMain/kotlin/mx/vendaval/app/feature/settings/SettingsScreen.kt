package mx.vendaval.app.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import mx.vendaval.app.i18n.LocalStrings
import mx.vendaval.app.presentation.CatalogueStateHolder
import mx.vendaval.app.presentation.PreferencesStateHolder
import mx.vendaval.app.theme.VendavalTheme
import mx.vendaval.app.ui.component.SyncBadge
import mx.vendaval.core.data.repository.AppLanguage
import mx.vendaval.core.data.repository.ThemeMode
import mx.vendaval.core.data.seed.SeedCatalogue
import mx.vendaval.core.platform.currentPlatform

/**
 * Appearance, data and provenance.
 *
 * The platform label and the seeded-catalogue count are here on purpose: this is the
 * one screen where a user reporting a problem can read back what build they are on and
 * how much data the app carries without a network.
 *
 * ## Why it no longer uses chips
 * Settings references (Wispr Flow, WeChat) put the **section label outside** the card
 * and the **current value on the right of each row**. The previous version did the
 * opposite: the section title sat inside the card at `titleLarge`, competing with the
 * settings it introduced, and both preferences were expressed as `FilterChip` rows.
 * A chip row is a *filter* idiom — it implies a multi-select over a set that may grow.
 * Theme and language are single-select over a fixed, tiny set, which is what a
 * segmented control means, so that is what they are now.
 */
@Composable
fun SettingsScreen(
    catalogue: CatalogueStateHolder,
    preferences: PreferencesStateHolder,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(16.dp),
) {
    val strings = LocalStrings.current
    val colours = VendavalTheme.colors
    val current by preferences.preferences.collectAsState()
    val syncState by catalogue.syncState.collectAsState()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item(key = "title") {
            Text(
                text = strings.navSettings,
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        item(key = "appearance") {
            SettingsGroup(strings.settingsAppearance) {
                StackedRow(strings.settingsTheme) {
                    Segmented(
                        options = ThemeMode.entries.map { mode ->
                            mode to when (mode) {
                                ThemeMode.SYSTEM -> strings.settingsThemeSystem
                                ThemeMode.DARK -> strings.settingsThemeDark
                                ThemeMode.LIGHT -> strings.settingsThemeLight
                            }
                        },
                        selected = current.themeMode,
                        onSelect = preferences::setThemeMode,
                    )
                }
                RowDivider()
                StackedRow(strings.settingsLanguage) {
                    Segmented(
                        options = AppLanguage.entries.map { it to it.tag.uppercase() },
                        selected = current.language,
                        onSelect = preferences::setLanguage,
                    )
                }
            }
        }

        item(key = "data") {
            SettingsGroup(strings.settingsData) {
                // Labelled "Estado", not "Datos": the section heading directly above
                // already says Datos, and repeating it left the same word stacked on
                // itself. A row label should name the row, not its container.
                ValueRow(label = strings.settingsSyncStatus) { SyncBadge(syncState) }
                RowDivider()
                ActionRow(
                    icon = Icons.Outlined.Refresh,
                    label = strings.settingsRefresh,
                    enabled = !syncState.isBusy,
                    onClick = catalogue::onRefresh,
                )
                RowDivider()
                ValueRow(label = strings.settingsSourceCatalogue(SeedCatalogue.spaces.size))
            }
        }

        item(key = "about") {
            SettingsGroup(strings.settingsAbout) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = strings.settingsAboutBody,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colours.textMuted,
                    )
                    Text(
                        text = strings.mapAttribution,
                        style = MaterialTheme.typography.bodySmall,
                        color = colours.textFaint,
                    )
                }
                RowDivider()
                ValueRow(label = strings.settingsPlatform) {
                    Text(
                        text = currentPlatform().label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colours.textMuted,
                    )
                }
            }
        }

        item(key = "spacer") {
            Spacer(Modifier.height(20.dp))
        }
    }
}

/** Section label outside, rows inside. The label introduces; it does not compete. */
@Composable
private fun SettingsGroup(heading: String, content: @Composable ColumnScope.() -> Unit) {
    val colours = VendavalTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Text(
            // Uppercase, tracked caption — the grouped-settings section-header idiom
            // the iOS references (Qonto, MoonPay) use. Reads as a deliberate label
            // rather than body text that merely happens to be small.
            text = heading.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = colours.textMuted,
            modifier = Modifier.padding(start = 4.dp),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(VendavalTheme.radii.medium))
                .background(colours.card),
            content = content,
        )
    }
}

@Composable
private fun RowDivider() {
    // Inset to the row's content edge, not full-bleed: grouped lists in the
    // references slice between rows without cutting the card clean in two.
    Box(
        Modifier
            .fillMaxWidth()
            .padding(start = 14.dp)
            .height(1.dp)
            .background(VendavalTheme.colors.line),
    )
}

/** Label above its control. Used when the control is too wide to sit beside a label. */
@Composable
private fun StackedRow(label: String, control: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        control()
    }
}

/** Label left, current value right. */
@Composable
private fun ValueRow(label: String, value: (@Composable () -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (value == null) {
                VendavalTheme.colors.textMuted
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            modifier = Modifier.weight(1f, fill = false),
        )
        value?.invoke()
    }
}

/** A row that does something. Tinted with the accent so it reads as interactive. */
@Composable
private fun ActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val colours = VendavalTheme.colors
    val tint = if (enabled) colours.accent else colours.textFaint

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = tint,
        )
    }
}

/**
 * Single-select over a fixed, small set.
 *
 * Built by hand rather than with `SegmentedButton` so the selected segment can use the
 * app's own surface ramp — the Material default paints the selection in the primary
 * container colour, which would spend the reserved accent on a preference row.
 */
@Composable
private fun <T> Segmented(
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit,
) {
    val colours = VendavalTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(VendavalTheme.radii.small))
            .background(colours.surfaceSunken)
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        options.forEach { (value, label) ->
            val isSelected = value == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(VendavalTheme.radii.small - 3.dp))
                    // `cardRaised` on `surfaceSunken` was too close a pair on the
                    // four-step ramp to answer "which one is on?" at a glance. The
                    // selected segment now also carries a hairline, which separates
                    // it structurally rather than leaning on two adjacent greys.
                    .background(if (isSelected) colours.cardRaised else colours.surfaceSunken)
                    .then(
                        if (isSelected) {
                            Modifier.border(
                                width = 1.dp,
                                color = colours.lineStrong,
                                shape = RoundedCornerShape(VendavalTheme.radii.small - 3.dp),
                            )
                        } else {
                            Modifier
                        },
                    )
                    .clickable(role = Role.RadioButton) { onSelect(value) }
                    .semantics { this.selected = isSelected }
                    .padding(vertical = 9.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        colours.textFaint
                    },
                )
            }
        }
    }
}
