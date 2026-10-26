package mx.vendaval.app.ui.component

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import mx.vendaval.app.i18n.LocalStrings
import mx.vendaval.app.theme.VendavalTheme
import mx.vendaval.app.theme.accentFor
import mx.vendaval.core.domain.model.SpaceOrigin
import mx.vendaval.core.domain.model.SyncFailure
import mx.vendaval.core.domain.model.SyncState
import mx.vendaval.core.domain.model.VenueType

/**
 * Venue-type chip.
 *
 * Always carries icon *and* text alongside the colour. The web version encodes the
 * type in hue alone, which is invisible to a colour-blind user and disappears
 * entirely in greyscale.
 */
@Composable
fun VenueTypeBadge(
    type: VenueType,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val accent = accentFor(type)
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(accent.colour.copy(alpha = 0.14f))
            .border(1.dp, accent.colour.copy(alpha = 0.45f), RoundedCornerShape(999.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = accent.icon,
            contentDescription = null,
            tint = accent.colour,
            modifier = Modifier.size(13.dp),
        )
        Text(
            text = if (compact) type.shortLabel else type.label,
            style = MaterialTheme.typography.labelSmall,
            color = accent.colour,
        )
    }
}

/** Marks records that exist only on this device and have not been published. */
@Composable
fun PendingUploadBadge(origin: SpaceOrigin, modifier: Modifier = Modifier) {
    if (origin != SpaceOrigin.LOCAL) return
    val strings = LocalStrings.current
    val colours = VendavalTheme.colors

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(colours.warning.copy(alpha = 0.16f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        PulsingDot(colours.warning)
        Text(
            text = strings.detailPendingUpload,
            style = MaterialTheme.typography.labelSmall,
            color = colours.warning,
        )
    }
}

/**
 * Sync status.
 *
 * Deliberately non-modal and never blocking: a failed sync is annotation on data
 * that is already usable, not an error dialog. The failure cause selects the
 * wording so "you are offline" never reads as "the server is broken".
 */
@Composable
fun SyncBadge(state: SyncState, modifier: Modifier = Modifier) {
    val strings = LocalStrings.current
    val colours = VendavalTheme.colors

    val (label, tint) = when (state) {
        SyncState.Idle -> strings.syncIdle to colours.textFaint
        SyncState.Syncing -> strings.syncSyncing to colours.accent
        is SyncState.Synced -> strings.syncUpToDate(state.remoteCount) to colours.textFaint
        is SyncState.Failed -> when (state.cause) {
            // Being offline while showing a complete cached catalogue is
            // information, not a problem: the app works. Amber here sat directly
            // under the masthead and pulled attention away from the content, so
            // it is toned down to match the other secondary metadata. Genuine
            // faults — a rejection, a server error — keep their colour.
            SyncFailure.OFFLINE -> strings.syncOffline to colours.textMuted
            SyncFailure.REJECTED -> strings.syncRejected to colours.danger
            SyncFailure.SERVER -> strings.syncServer to colours.danger
            SyncFailure.MALFORMED -> strings.syncMalformed to colours.danger
            SyncFailure.UNKNOWN -> strings.syncUnknown to colours.warning
        }
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (state is SyncState.Syncing) PulsingDot(tint) else SolidDot(tint)
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = tint)
    }
}

/** Ported from the `pulse-dot` animation on the original page. */
@Composable
fun PulsingDot(colour: Color, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val alpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1_100),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse-alpha",
    )

    Box(
        modifier = modifier
            .size(8.dp)
            .alpha(alpha)
            .clip(CircleShape)
            .background(colour)
            // The animation is decorative; screen readers get the adjacent label.
            .clearAndSetSemantics { },
    )
}

@Composable
private fun SolidDot(colour: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(colour)
            .clearAndSetSemantics { },
    )
}
