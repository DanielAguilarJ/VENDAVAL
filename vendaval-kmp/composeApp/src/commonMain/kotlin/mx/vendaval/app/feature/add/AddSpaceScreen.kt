package mx.vendaval.app.feature.add

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.material.icons.outlined.Add
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.launch
import mx.vendaval.app.ui.map.decodeImageBytes
import mx.vendaval.app.i18n.LocalStrings
import mx.vendaval.app.theme.VendavalTheme
import mx.vendaval.app.theme.accentFor
import mx.vendaval.app.ui.component.FieldGroup
import mx.vendaval.app.ui.component.PickerRow
import mx.vendaval.app.ui.component.VendavalTextField
import mx.vendaval.core.domain.model.DraftField
import mx.vendaval.core.domain.model.Municipality
import mx.vendaval.core.domain.model.VenueType

/**
 * Community submission form.
 *
 * Two behaviours are worth calling out because they are the difference between a
 * form people finish and one they abandon:
 *  - coordinates are hard to obtain, so there are two shortcuts (device location and
 *    municipal centre) rather than an empty pair of numeric fields;
 *  - saving succeeds offline. The record is durable locally and publishes later, so a
 *    dead connection never discards the work the user just did.
 *
 * ## Why the layout changed
 * Form references (eBay, Qonto) do three things this screen was not doing. They
 * **group** fields under headings, so a nine-field form reads as three short
 * questions instead of one long list. They **pin the primary action** to the bottom
 * edge, because in a scrolling form the submit button used to disappear upward and
 * take the error summary with it. And they **collapse long choices behind a row** —
 * eleven municipalities as chips wrapped across four lines and buried the fields
 * below them.
 */
@Composable
fun AddSpaceScreen(
    holder: AddSpaceStateHolder,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalStrings.current
    val colours = VendavalTheme.colors
    val validation = holder.validation
    val showErrors = holder.showErrors

    var municipalityOpen by remember { mutableStateOf(false) }

    // On success, show a confirmation instead of vanishing — the user needs to see the
    // record landed and that it is already in the directory and the map.
    (holder.submitState as? SubmitState.Saved)?.let { saved ->
        SuccessPanel(
            published = saved.published,
            onDone = onDismiss,
            onAddAnother = holder::reset,
            modifier = modifier,
        )
        return
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 20.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = strings.formTitle,
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = strings.formSubtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colours.textMuted,
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Outlined.Close,
                        contentDescription = strings.formCancel,
                        tint = colours.textMuted,
                    )
                }
            }

            FieldGroup(strings.formGroupIdentity) {
                VendavalTextField(
                    value = holder.draft.name,
                    onValueChange = holder::onNameChange,
                    label = strings.formName,
                    error = validation.errors[DraftField.NAME].takeIf { showErrors },
                )
                TypePicker(
                    label = strings.formType,
                    selected = holder.draft.type,
                    onSelect = holder::onTypeChange,
                    error = validation.errors[DraftField.TYPE].takeIf { showErrors },
                )
            }

            FieldGroup(strings.formGroupLocation) {
                VendavalTextField(
                    value = holder.draft.address,
                    onValueChange = holder::onAddressChange,
                    label = strings.formAddress,
                    error = validation.errors[DraftField.ADDRESS].takeIf { showErrors },
                )

                PickerRow(
                    label = strings.formMunicipality,
                    value = holder.draft.municipality?.label,
                    placeholder = strings.formMunicipalityPlaceholder,
                    onClick = { municipalityOpen = !municipalityOpen },
                    error = validation.errors[DraftField.MUNICIPALITY].takeIf { showErrors },
                    leadingIcon = Icons.Outlined.Place,
                )

                // Expands in place rather than opening a modal: the list is eleven
                // items long, so a sheet would be more machinery than the choice needs.
                AnimatedVisibility(visible = municipalityOpen) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(VendavalTheme.radii.small))
                            .background(colours.surfaceSunken),
                    ) {
                        Municipality.entries.forEach { municipality ->
                            MunicipalityRow(
                                label = municipality.label,
                                selected = holder.draft.municipality == municipality,
                                onClick = {
                                    holder.onMunicipalityChange(municipality)
                                    municipalityOpen = false
                                },
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    VendavalTextField(
                        value = holder.draft.latitude,
                        onValueChange = holder::onLatitudeChange,
                        label = strings.formLatitude,
                        error = validation.errors[DraftField.LATITUDE].takeIf { showErrors },
                        warning = validation.warnings[DraftField.LATITUDE],
                        keyboardType = KeyboardType.Decimal,
                        modifier = Modifier.weight(1f),
                    )
                    VendavalTextField(
                        value = holder.draft.longitude,
                        onValueChange = holder::onLongitudeChange,
                        label = strings.formLongitude,
                        error = validation.errors[DraftField.LONGITUDE].takeIf { showErrors },
                        keyboardType = KeyboardType.Decimal,
                        modifier = Modifier.weight(1f),
                    )
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (holder.supportsLocation) {
                        ShortcutButton(
                            icon = Icons.Outlined.MyLocation,
                            label = strings.formUseMyLocation,
                            busy = holder.isLocating,
                            enabled = !holder.isLocating,
                            onClick = holder::useCurrentLocation,
                        )
                    }
                    ShortcutButton(
                        icon = Icons.Outlined.Place,
                        label = strings.formUseMunicipalityCentre,
                        enabled = holder.draft.municipality != null,
                        onClick = holder::useMunicipalityCentre,
                    )
                }
            }

            FieldGroup(strings.formGroupContact) {
                VendavalTextField(
                    value = holder.draft.phone,
                    onValueChange = holder::onPhoneChange,
                    label = strings.formPhone,
                    optionalLabel = strings.formPhoneOptional,
                    error = validation.errors[DraftField.PHONE].takeIf { showErrors },
                    keyboardType = KeyboardType.Phone,
                )
                VendavalTextField(
                    value = holder.draft.website,
                    onValueChange = holder::onWebsiteChange,
                    label = strings.formWebsite,
                    optionalLabel = strings.formPhoneOptional,
                    error = validation.errors[DraftField.WEBSITE].takeIf { showErrors },
                    keyboardType = KeyboardType.Uri,
                )
                VendavalTextField(
                    value = holder.draft.mapQuery,
                    onValueChange = holder::onMapQueryChange,
                    label = strings.formMapQuery,
                    optionalLabel = strings.formPhoneOptional,
                )
            }

            FieldGroup(strings.formGroupAbout) {
                VendavalTextField(
                    value = holder.draft.description,
                    onValueChange = holder::onDescriptionChange,
                    label = strings.formDescription,
                    optionalLabel = strings.formPhoneOptional,
                    error = validation.errors[DraftField.DESCRIPTION].takeIf { showErrors },
                    singleLine = false,
                    minLines = 3,
                )
                VendavalTextField(
                    value = holder.draft.hours,
                    onValueChange = holder::onHoursChange,
                    label = strings.formHours,
                    optionalLabel = strings.formPhoneOptional,
                    error = validation.errors[DraftField.HOURS].takeIf { showErrors },
                )
            }

            FieldGroup(strings.formGroupSocial) {
                mx.vendaval.core.domain.model.SocialPlatform.entries.forEach { platform ->
                    VendavalTextField(
                        value = holder.draft.socialLinks[platform].orEmpty(),
                        onValueChange = { holder.onSocialChange(platform, it) },
                        label = platform.label,
                        optionalLabel = strings.formPhoneOptional,
                    )
                }
                validation.errors[DraftField.SOCIAL].takeIf { showErrors }?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = colours.danger,
                    )
                }
            }

            PhotosSection(holder = holder)

            Spacer(Modifier.height(4.dp))
        }

        // Pinned action bar. The rejection reason lives here too, beside the button
        // that triggered it, instead of scrolled off the top of the form.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colours.surfaceSunken)
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            (holder.submitState as? SubmitState.Rejected)?.let { rejected ->
                Text(
                    text = rejected.reason,
                    style = MaterialTheme.typography.bodySmall,
                    color = colours.danger,
                )
            }

            val submitting = holder.submitState is SubmitState.Submitting

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(VendavalTheme.radii.medium))
                    .background(if (submitting) colours.cardRaised else colours.accent)
                    .clickable(
                        enabled = !submitting,
                        role = Role.Button,
                        onClick = holder::submit,
                    )
                    .padding(vertical = 15.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (submitting) {
                    CircularProgressIndicator(
                        color = colours.accent,
                        modifier = Modifier.size(19.dp),
                    )
                } else {
                    Text(
                        text = strings.formSubmit,
                        style = MaterialTheme.typography.bodyLarge,
                        // Dark ink on the accent: the accent is light enough that
                        // white text on it fails contrast at body sizes.
                        color = colours.surfaceSunken,
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(VendavalTheme.radii.medium))
                    .clickable(role = Role.Button, onClick = onDismiss)
                    .padding(vertical = 11.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = strings.formCancel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colours.textMuted,
                )
            }
        }
    }
}

/**
 * Venue type as a chip row.
 *
 * Kept as chips, unlike the municipality list: there are six types with short labels,
 * they fit in two rows, and each one carries its own hue — which makes the selected
 * chip teach the colour the user will then see on cards and map pins. That mapping is
 * worth the space; eleven place names were not.
 */
@Composable
private fun TypePicker(
    label: String,
    selected: VenueType?,
    onSelect: (VenueType) -> Unit,
    error: String?,
) {
    val colours = VendavalTheme.colors

    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            VenueType.selectable.forEach { type ->
                val accent = accentFor(type)
                val isSelected = selected == type
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(
                            if (isSelected) accent.colour.copy(alpha = 0.18f) else colours.card,
                        )
                        .border(
                            width = 1.dp,
                            color = if (isSelected) accent.colour else colours.line,
                            shape = CircleShape,
                        )
                        .clickable(role = Role.RadioButton) { onSelect(type) }
                        .padding(horizontal = 13.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    Box(
                        Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(accent.colour),
                    )
                    Text(
                        text = type.shortLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            colours.textMuted
                        },
                    )
                }
            }
        }
        error?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = colours.danger,
            )
        }
    }
}

@Composable
private fun MunicipalityRow(label: String, selected: Boolean, onClick: () -> Unit) {
    val colours = VendavalTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 13.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        if (selected) {
            Icon(
                Icons.Outlined.Check,
                contentDescription = null,
                tint = colours.accent,
                modifier = Modifier.size(17.dp),
            )
        }
    }
}

/** A way to fill the coordinate fields without typing numbers. */
@Composable
private fun ShortcutButton(
    icon: ImageVector,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    busy: Boolean = false,
) {
    val colours = VendavalTheme.colors
    val tint = if (enabled) colours.accent else colours.textFaint

    Row(
        modifier = Modifier
            .clip(CircleShape)
            .border(1.dp, if (enabled) colours.lineStrong else colours.line, CircleShape)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (busy) {
            CircularProgressIndicator(color = tint, modifier = Modifier.size(15.dp))
        } else {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = tint,
        )
    }
}

/**
 * Optional photo attachments for a submission.
 *
 * Only ever shows images the user chose themselves — real venue photography is never
 * invented. The chooser comes from [LocalPhotoPicker]; on a platform without one wired
 * the add tile simply yields nothing and the section stays a harmless empty affordance.
 */
@Composable
private fun PhotosSection(holder: AddSpaceStateHolder) {
    val strings = LocalStrings.current
    val colours = VendavalTheme.colors
    val picker = LocalPhotoPicker.current
    val scope = rememberCoroutineScope()

    FieldGroup(strings.formStepPhotosTitle) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            holder.pickedPhotos.forEachIndexed { index, photo ->
                val bitmap = remember(photo) { decodeImageBytes(photo.bytes) }
                Box(
                    Modifier
                        .size(84.dp)
                        .clip(RoundedCornerShape(VendavalTheme.radii.small))
                        .background(colours.card)
                        .border(1.dp, colours.line, RoundedCornerShape(VendavalTheme.radii.small)),
                ) {
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap,
                            contentDescription = photo.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .matchParentSize()
                                .clip(RoundedCornerShape(VendavalTheme.radii.small)),
                        )
                    }
                    Box(
                        Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(colours.surfaceSunken)
                            .clickable(role = Role.Button) { holder.removePhoto(index) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Outlined.Close,
                            contentDescription = strings.formCancel,
                            tint = colours.textMuted,
                            modifier = Modifier.size(14.dp),
                        )
                    }
                }
            }

            Box(
                Modifier
                    .size(84.dp)
                    .clip(RoundedCornerShape(VendavalTheme.radii.small))
                    .background(colours.card)
                    .border(1.dp, colours.lineStrong, RoundedCornerShape(VendavalTheme.radii.small))
                    .clickable(role = Role.Button) {
                        scope.launch { picker.pick()?.let(holder::addPhoto) }
                    },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Outlined.Add,
                    contentDescription = null,
                    tint = colours.accent,
                    modifier = Modifier.size(24.dp),
                )
            }
        }

        Text(
            text = strings.formPhotosHint,
            style = MaterialTheme.typography.bodySmall,
            color = colours.textMuted,
        )
    }
}

/**
 * Post-submit confirmation. Replaces the old auto-dismiss so the contribution's
 * success is visible feedback: it names whether the record published or is queued
 * offline, and offers "add another" (reset) alongside done (dismiss).
 */
@Composable
private fun SuccessPanel(
    published: Boolean,
    onDone: () -> Unit,
    onAddAnother: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalStrings.current
    val colours = VendavalTheme.colors

    Column(
        modifier = modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(colours.accent.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Outlined.Check,
                contentDescription = null,
                tint = colours.accent,
                modifier = Modifier.size(30.dp),
            )
        }
        Text(
            text = strings.formSavedTitle,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = if (published) strings.formSavedPublished else strings.formSavedOffline,
            style = MaterialTheme.typography.bodyMedium,
            color = colours.textMuted,
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(VendavalTheme.radii.medium))
                .background(colours.accent)
                .clickable(role = Role.Button, onClick = onDone)
                .padding(vertical = 15.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = strings.actionClose,
                style = MaterialTheme.typography.bodyLarge,
                color = colours.surfaceSunken,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(VendavalTheme.radii.medium))
                .clickable(role = Role.Button, onClick = onAddAnother)
                .padding(vertical = 11.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = strings.formAddAnother,
                style = MaterialTheme.typography.bodyMedium,
                color = colours.textMuted,
            )
        }
    }
}
