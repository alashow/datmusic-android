/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.ContentAlpha
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.VectorPainter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import coil.compose.AsyncImagePainter.State
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.google.accompanist.placeholder.PlaceholderDefaults
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.color
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.shimmer

@Composable
fun CoverImage(
    data: Any?,
    modifier: Modifier = Modifier,
    imageModifier: Modifier = Modifier,
    size: Dp = Dp.Unspecified,
    // TODO: remove when placeholder uses M3
    backgroundColor: Color = PlaceholderDefaults.color(backgroundColor = MaterialTheme.colorScheme.surface),
    contentColor: Color = MaterialTheme.colorScheme.secondary,
    contentScale: ContentScale = ContentScale.Crop,
    shape: Shape = MaterialTheme.shapes.small,
    icon: VectorPainter = rememberVectorPainter(Icons.Default.MusicNote),
    iconPadding: Dp = if (size != Dp.Unspecified) size * 0.25f else 24.dp,
    bitmapPlaceholder: Bitmap? = null,
    contentDescription: String? = null,
    elevation: Dp = 2.dp,
) {
    val sizeMod = if (size.isSpecified) Modifier.size(size) else Modifier
    Surface(
        tonalElevation = elevation,
        shape = shape,
        color = backgroundColor,
        modifier = modifier
            .then(sizeMod)
            .aspectRatio(1f)
    ) {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(data)
                .build(),
            contentDescription = contentDescription,
            contentScale = contentScale,
        ) {
            val state = painter.state
            when (state) {
                is State.Error, State.Empty, is State.Loading -> {
                    Icon(
                        painter = icon,
                        tint = contentColor.copy(alpha = ContentAlpha.disabled),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(backgroundColor)
                            .padding(iconPadding)
                    )
                    Box(
                        Modifier
                            .fillMaxSize()
                            .placeholder(
                                visible = state is State.Loading,
                                color = Color.Transparent,
                                shape = shape,
                                highlight = PlaceholderHighlight.shimmer(highlightColor = contentColor.copy(alpha = .15f)),
                            )
                    )
                }
                else -> SubcomposeAsyncImageContent(imageModifier.fillMaxSize())
            }

            if (bitmapPlaceholder != null && state is State.Loading) {
                Image(
                    painter = rememberAsyncImagePainter(bitmapPlaceholder),
                    contentDescription = null,
                    contentScale = contentScale,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(shape)
                )
            }
        }
    }
}
