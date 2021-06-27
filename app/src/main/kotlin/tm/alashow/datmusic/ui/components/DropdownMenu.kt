/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun <T> SelectableDropdownMenu(items: List<T>, selectedItem: T, onItemSelect: (T) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Column(Modifier.clickable { expanded = !expanded }) {
        Text(selectedItem.toString())
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.width(200.dp)
        ) {
            items.forEach { item ->
                val label = item.toString()
                DropdownMenuItem(
                    onClick = {
                        expanded = !expanded
                        onItemSelect(item)
                    }
                ) {
                    Text(text = label, color = if (selectedItem == item) MaterialTheme.colors.secondary else MaterialTheme.colors.onBackground)
                }
            }
        }
    }
}
