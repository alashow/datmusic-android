/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.library.items

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tm.alashow.datmusic.domain.entities.LibraryItem
import tm.alashow.ui.components.CoverImage
import tm.alashow.ui.material.ContentAlpha
import tm.alashow.ui.material.ProvideContentAlpha
import tm.alashow.ui.theme.AppTheme

internal object LibraryItemRowDefaults {
    val imageSize = 56.dp
}

@Composable
internal fun LibraryItemRow(
    libraryItem: LibraryItem,
    @StringRes typeRes: Int,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onImageClick: (() -> Unit)? = null,
    imageSize: Dp = LibraryItemRowDefaults.imageSize,
    imageData: Any? = null,
    actionHandler: LibraryItemActionHandler = {},
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(AppTheme.specs.padding),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clickable {
                if (onClick != null)
                    onClick()
            }
            .fillMaxWidth()
            .padding(AppTheme.specs.inputPaddings)
    ) {
        LibraryItemRow(imageSize, imageData, onImageClick, onClick, libraryItem, typeRes, modifier = Modifier.weight(19f))

        var menuVisible by remember { mutableStateOf(false) }
        LibraryItemDropdownMenu(
            libraryItem = libraryItem,
            expanded = menuVisible,
            onExpandedChange = { menuVisible = it },
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically),
            onDropdownSelect = {
                val action = LibraryItemAction.from(it, libraryItem)
                actionHandler(action)
            },
        )
    }
}

@Composable
private fun LibraryItemRow(
    imageSize: Dp,
    imageData: Any?,
    onImageClick: (() -> Unit)?,
    onClick: (() -> Unit)?,
    libraryItem: LibraryItem,
    typeRes: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(AppTheme.specs.padding),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        CoverImage(
            data = imageData,
            size = imageSize,
            imageModifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { (onImageClick ?: onClick)?.invoke() },
            ),
        )

        Column(verticalArrangement = Arrangement.spacedBy(AppTheme.specs.paddingTiny)) {
            Text(
                libraryItem.getLabel(),
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            ProvideContentAlpha(ContentAlpha.medium) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AppTheme.specs.paddingTiny),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        stringResource(typeRes),
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.alignByBaseline()
                    )
                }
            }
        }
    }
}
