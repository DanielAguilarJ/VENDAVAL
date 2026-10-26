package mx.vendaval.app.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import mx.vendaval.app.theme.VendavalTheme

/**
 * Form controls drawn from the app's own surface ramp.
 *
 * Material's `OutlinedTextField` paints its focused outline and floating label with
 * the colour scheme's *primary*, which in this theme is the single reserved accent.
 * Nine fields lit in accent would spend the one colour that is supposed to mean
 * "you can interact with this specific thing" on ordinary text entry. These controls
 * use the surface ramp for rest state and reserve the accent for focus alone, which
 * is the one moment it is actually true.
 */

/**
 * A labelled group of related fields.
 *
 * Grouping matters more in a long form than in a short one: nine fields presented as
 * a flat list give the reader no sense of how much is left or how the parts relate.
 * Identity, location and contact are three separate questions.
 *
 * The parameter is called [heading], not `label`, on purpose. When both this and
 * [VendavalTextField] took a parameter named `label`, every call site in the form was
 * written with the field's own name in both — producing "Dirección" stacked directly
 * on "Dirección" three times over. Different names for different levels makes the
 * mistake visible while writing it.
 */
@Composable
fun FieldGroup(
    heading: String,
    modifier: Modifier = Modifier,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        Text(
            text = heading,
            style = MaterialTheme.typography.labelSmall,
            color = VendavalTheme.colors.textFaint,
        )
        content()
    }
}

/**
 * Single-line text entry.
 *
 * The label sits **above** the field rather than floating inside it. A floating label
 * animates away on focus, which means the question disappears exactly when the user
 * is answering it — fine for one obvious field, actively unhelpful in a form where
 * "Latitud" and "Longitud" sit side by side and look identical once emptied of labels.
 */
@Composable
fun VendavalTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    error: String? = null,
    warning: String? = null,
    optionalLabel: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    minLines: Int = 1,
) {
    val colours = VendavalTheme.colors
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()

    val borderColour by animateColorAsState(
        targetValue = when {
            error != null -> colours.danger
            focused -> colours.accent
            else -> colours.line
        },
        label = "field-border",
    )

    val selectionColours = TextSelectionColors(
        handleColor = colours.accent,
        backgroundColor = colours.accent.copy(alpha = 0.28f),
    )

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            optionalLabel?.let { mark ->
                Text(
                    text = "·",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colours.textFaint,
                )
                Text(
                    text = mark,
                    style = MaterialTheme.typography.labelSmall,
                    color = colours.textFaint,
                )
            }
        }

        CompositionLocalProvider(LocalTextSelectionColors provides selectionColours) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                ),
                cursorBrush = SolidColor(colours.accent),
                singleLine = singleLine,
                minLines = minLines,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                interactionSource = interaction,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(VendavalTheme.radii.small))
                    .background(colours.card)
                    .border(
                        width = 1.dp,
                        color = borderColour,
                        shape = RoundedCornerShape(VendavalTheme.radii.small),
                    )
                    .padding(horizontal = 13.dp, vertical = 13.dp),
                decorationBox = { field ->
                    Box {
                        if (value.isEmpty() && placeholder != null) {
                            Text(
                                text = placeholder,
                                style = MaterialTheme.typography.bodyLarge,
                                color = colours.textFaint,
                            )
                        }
                        field()
                    }
                },
            )
        }

        // Errors take precedence over warnings: no point advising on a value that
        // is not yet acceptable.
        when {
            error != null -> Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = colours.danger,
            )

            warning != null -> Text(
                text = warning,
                style = MaterialTheme.typography.bodySmall,
                color = colours.warning,
            )
        }
    }
}

/**
 * A row that opens a picker instead of expanding inline.
 *
 * Used for choices too numerous to lay out flat. Eleven municipalities as chips took
 * three or four wrapped lines and turned one field into a wall; as a row it takes one
 * line and shows the current answer, which is what the reader actually wants to see
 * when reviewing a form before submitting it.
 */
@Composable
fun PickerRow(
    label: String,
    value: String?,
    placeholder: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null,
    leadingIcon: ImageVector? = null,
) {
    val colours = VendavalTheme.colors

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(VendavalTheme.radii.small))
                .background(colours.card)
                .border(
                    width = 1.dp,
                    color = if (error != null) colours.danger else colours.line,
                    shape = RoundedCornerShape(VendavalTheme.radii.small),
                )
                .clickable(role = Role.Button, onClick = onClick)
                .padding(horizontal = 13.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            leadingIcon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = colours.textFaint,
                    modifier = Modifier.size(17.dp),
                )
            }
            Text(
                text = value ?: placeholder,
                style = MaterialTheme.typography.bodyLarge,
                color = if (value == null) colours.textFaint else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = colours.textFaint,
                modifier = Modifier.size(19.dp),
            )
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
