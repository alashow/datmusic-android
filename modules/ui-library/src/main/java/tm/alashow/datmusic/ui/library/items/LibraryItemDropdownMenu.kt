/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.library.items

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import tm.alashow.datmusic.domain.entities.LibraryItem
import tm.alashow.datmusic.ui.library.R
import tm.alashow.ui.components.MoreVerticalIcon

@Composable
internal fun LibraryItemDropdownMenu(
    libraryItem: LibraryItem,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    onDropdownSelect: (actionLabelRes: Int) -> Unit
) {
    val items = buildList {
        if (libraryItem.isUpdatable)
            add(R.string.library_item_menu_edit)
        if (libraryItem.isDownloadable)
            add(R.string.library_item_menu_download)
        if (libraryItem.isDeletable)
            add(R.string.library_item_menu_delete)
    }

    if (items.isNotEmpty()) {
        MoreVerticalIcon(onClick = { onExpandedChange(true) }, modifier = modifier)

        Box {
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { onExpandedChange(false) },
                modifier = Modifier
                    .width(IntrinsicSize.Min)
                    .align(Alignment.Center)
            ) {
                items.forEach { item ->
                    val label = stringResource(item)
                    DropdownMenuItem(
                        onClick = {
                            onExpandedChange(false)
                            onDropdownSelect(item)
                        },
                        text = { Text(text = label) }
                    )
                }
            }
        }
    }
}
