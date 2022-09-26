/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import tm.alashow.ui.material.ContentAlpha
import tm.alashow.ui.material.ProvideContentAlpha
import tm.alashow.ui.theme.Theme

@Composable
fun <T> SelectableDropdownMenu(
    items: List<T>,
    selectedItem: T,
    onItemSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    selectedItems: Set<T> = setOf(selectedItem),
    multipleSelectionsLabel: @Composable (Set<T>) -> String = { _ -> stringResource(R.string.dropdown_selected_multiple) },
    itemLabelMapper: @Composable (T) -> String = { it.toString().replace("_", " ") },
    itemSuffixMapper: @Composable (RowScope.(T) -> Unit)? = null,
    subtitles: List<String?>? = null,
    leadingIcon: ImageVector? = null,
    iconOnly: Boolean = false,
    leadingIconColor: Color = LocalContentColor.current,
    border: BorderStroke? = null,
) {
    var expanded by remember { mutableStateOf(false) }
    val dropIcon = if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown

    Column(modifier) {
        AppOutlinedButton(
            onClick = { expanded = !expanded },
            colors = ButtonDefaults.textButtonColors(contentColor = LocalContentColor.current),
            contentPadding = PaddingValues(
                start = Theme.specs.padding,
                end = if (iconOnly) Theme.specs.padding else Theme.specs.paddingSmall,
                top = Theme.specs.paddingSmall,
                bottom = Theme.specs.paddingSmall,
            ),
            border = border,
        ) {
            if (leadingIcon != null) {
                Icon(
                    painter = rememberVectorPainter(leadingIcon),
                    contentDescription = null,
                    modifier = Modifier
                        .width(Theme.specs.iconSizeTiny),
                    tint = leadingIconColor,
                )
                if (!iconOnly) Spacer(Modifier.width(Theme.specs.paddingSmall))
            }
            if (!iconOnly) {
                val selectedText = when (selectedItems.size) {
                    0 -> "    "
                    1 -> itemLabelMapper(selectedItems.first())
                    else -> multipleSelectionsLabel(selectedItems)
                }
                Text(text = selectedText)
                Spacer(Modifier.width(Theme.specs.paddingSmall))
                Icon(painter = rememberVectorPainter(dropIcon), contentDescription = null)
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.width(IntrinsicSize.Min)
        ) {
            items.forEachIndexed { index, item ->
                DropdownMenuItem(
                    onClick = {
                        expanded = !expanded
                        onItemSelect(item)
                    },
                    text = {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                val isSelected = item in selectedItems
                                val contentColor = when {
                                    isSelected -> MaterialTheme.colorScheme.secondary
                                    else -> MaterialTheme.colorScheme.onSurface
                                }
                                CompositionLocalProvider(LocalContentColor provides contentColor) {
                                    Text(
                                        text = itemLabelMapper(item),
                                        style = MaterialTheme.typography.bodyMedium.run {
                                            if (isSelected) copy(fontWeight = FontWeight.Bold) else this
                                        },
                                    )
                                    if (itemSuffixMapper != null)
                                        itemSuffixMapper(item)
                                }
                            }

                            if (subtitles != null) {
                                val subtitle = subtitles[index]
                                if (subtitle != null)
                                    ProvideContentAlpha(ContentAlpha.medium) {
                                        Text(text = subtitle, style = MaterialTheme.typography.bodySmall)
                                    }
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun MoreVerticalIcon(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    rippleRadius: Dp = IconRippleRadiusMedium,
    contentDescription: String = stringResource(R.string.audio_menu_cd)
) {
    IconButton(
        onClick = onClick,
        rippleRadius = rippleRadius,
        modifier = modifier,
    ) {
        Icon(
            painter = rememberVectorPainter(Icons.Default.MoreVert),
            contentDescription = contentDescription,
        )
    }
}
