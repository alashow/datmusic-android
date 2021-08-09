/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ContentAlpha
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import tm.alashow.ui.theme.AppTheme

@Composable
fun <T> SelectableDropdownMenu(
    items: List<T>,
    selectedItem: T?,
    onItemSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    labelMapper: @Composable (T) -> String = { it.toString().replace("_", " ") },
    subtitles: List<String?>? = null,
) {
    var expanded by remember { mutableStateOf(false) }
    val dropIcon = if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown

    Column(modifier = modifier) {
        TextButton(
            onClick = { expanded = !expanded },
            colors = ButtonDefaults.textButtonColors(contentColor = LocalContentColor.current),
            contentPadding = PaddingValues(vertical = AppTheme.specs.paddingSmall, horizontal = AppTheme.specs.paddingSmall),
            modifier = Modifier.offset(x = 12.dp)
        ) {
            Text(text = if (selectedItem != null) labelMapper(selectedItem) else "    ")
            Spacer(Modifier.width(AppTheme.specs.paddingSmall))
            Icon(painter = rememberVectorPainter(dropIcon), contentDescription = "")
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
                    }
                ) {
                    Column {
                        Text(
                            text = labelMapper(item),
                            color = if (selectedItem == item) MaterialTheme.colors.secondary else MaterialTheme.colors.onBackground
                        )

                        if (subtitles != null) {
                            val subtitle = subtitles[index]
                            if (subtitle != null)
                                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                                    Text(text = subtitle, style = MaterialTheme.typography.caption)
                                }
                        }
                    }
                }
            }
        }
    }
}
