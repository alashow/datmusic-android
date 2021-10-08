/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.library.items

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import tm.alashow.base.imageloading.ImageLoading
import tm.alashow.datmusic.domain.entities.LibraryItem
import tm.alashow.ui.components.CoverImage
import tm.alashow.ui.theme.AppTheme

object LibraryItemRowDefaults {
    val imageSize = 56.dp
}

@Composable
fun LibraryItemRow(
    libraryItem: LibraryItem,
    @StringRes typeRes: Int,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    imageSize: Dp = LibraryItemRowDefaults.imageSize,
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
        val image = rememberImagePainter("", builder = ImageLoading.defaultConfig)
        CoverImage(
            painter = image,
            size = imageSize,
        ) { imageMod ->
            Image(
                painter = image,
                contentDescription = null,
                modifier = imageMod
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { },
                    )
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(AppTheme.specs.paddingTiny)) {
            Text(
                libraryItem.getLabel(),
                style = MaterialTheme.typography.body2.copy(fontSize = 15.sp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AppTheme.specs.paddingTiny),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        stringResource(typeRes),
                        style = MaterialTheme.typography.subtitle2,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.alignByBaseline()
                    )
                }
            }
        }
    }
}
