/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.VectorPainter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.accompanist.imageloading.ImageLoadState
import com.google.accompanist.imageloading.LoadPainter
import com.google.accompanist.placeholder.PlaceholderDefaults
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.fade
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.shimmer
import tm.alashow.ui.theme.AppTheme

@Composable
fun ImageWithPlaceholder(
    painter: LoadPainter<Any>,
    modifier: Modifier = Modifier,
    shape: Shape = RectangleShape,
    errorOrEmpty: @Composable () -> Unit = {},
    image: @Composable (Modifier) -> Unit
) {
    Box(
        modifier = modifier
    ) {
        image(
            Modifier
                .fillMaxSize()
                .placeholder(
                    visible = painter.loadState is ImageLoadState.Loading,
                    shape = shape,
                    highlight = PlaceholderHighlight.fade()
                )
        )

        when (painter.loadState) {
            is ImageLoadState.Error, ImageLoadState.Empty -> {
                errorOrEmpty()
            }
            else -> Unit
        }
    }
}

@Composable
fun CoverImage(
    painter: LoadPainter<Any>,
    size: Dp = 48.dp,
    backgroundColor: Color = MaterialTheme.colors.surface,
    shape: Shape = MaterialTheme.shapes.small,
    icon: VectorPainter = rememberVectorPainter(Icons.Default.MusicNote),
    iconPadding: Dp = AppTheme.specs.padding,
    modifier: Modifier = Modifier,
    image: @Composable (Modifier) -> Unit
) {
    Surface(
        elevation = 2.dp,
        shape = shape,
        color = backgroundColor,
        modifier = modifier.size(size)
    ) {
        image(
            Modifier
                .fillMaxSize()
                .clip(shape)
                .placeholder(
                    visible = painter.loadState is ImageLoadState.Loading,
                    highlight = PlaceholderHighlight.shimmer(
                        highlightColor = MaterialTheme.colors.secondary.copy(0.15f),
                        animationSpec = PlaceholderDefaults.shimmerAnimationSpec
                    ),
                )
        )

        when (painter.loadState) {
            is ImageLoadState.Error, ImageLoadState.Empty -> {
                Icon(
                    painter = icon,
                    tint = MaterialTheme.colors.secondary.copy(alpha = ContentAlpha.disabled),
                    contentDescription = "",
                    modifier = Modifier.padding(iconPadding)
                )
            }
            else -> Unit
        }
    }
}
