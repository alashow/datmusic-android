/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.accompanist.imageloading.ImageLoadState
import com.google.accompanist.imageloading.LoadPainter
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.material.shimmer
import tm.alashow.datmusic.ui.theme.AppTheme

@Composable
fun ImageWithPlaceholder(
    painter: LoadPainter<Any>,
    size: Dp = 50.dp,
    backgroundColor: Color = MaterialTheme.colors.surface,
    shape: Shape = MaterialTheme.shapes.small,
    icon: ImageVector = Icons.Default.MusicNote,
    iconPadding: Dp = AppTheme.specs.padding,
    modifier: Modifier = Modifier,
    image: @Composable (Modifier) -> Unit
) {
    Surface(
        elevation = 1.dp,
        shape = shape,
        color = backgroundColor,
        modifier = modifier
            .size(size)
    ) {
        image(
            Modifier
                .size(size)
                .clip(shape)
                .placeholder(
                    visible = painter.loadState is ImageLoadState.Loading,
                    highlight = PlaceholderHighlight.shimmer(),
                )
        )

        when (painter.loadState) {
            is ImageLoadState.Error, ImageLoadState.Empty -> {
                Icon(
                    painter = rememberVectorPainter(icon),
                    tint = MaterialTheme.colors.secondary.copy(alpha = ContentAlpha.disabled),
                    contentDescription = "",
                    modifier = Modifier.padding(iconPadding)
                )
            }
            else -> Unit
        }
    }
}
