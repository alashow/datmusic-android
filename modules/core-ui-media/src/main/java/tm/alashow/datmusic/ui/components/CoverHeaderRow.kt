/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.VectorPainter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tm.alashow.base.util.extensions.muteUntil
import tm.alashow.ui.components.CoverImage
import tm.alashow.ui.theme.AppTheme

object CoverHeaderDefaults {
    val height = 300.dp

    val titleStyle
        @Composable get() = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Black
        )
}

@Composable
fun CoverHeaderRow(
    title: String,
    modifier: Modifier = Modifier,
    titleModifier: Modifier = Modifier,
    imageData: Any? = null,
    icon: VectorPainter? = null,
    height: Dp = CoverHeaderDefaults.height,
    imageHeightFraction: Float = 0.85f,
    offsetProgress: State<Float> = mutableStateOf(0f),
    titleStyle: TextStyle = CoverHeaderDefaults.titleStyle,
) {
    // scale down as the header is scrolled away
    val imageAlpha = 1 - (offsetProgress.value.muteUntil(0.5f) * 2f)
    val imageScale = 1 - offsetProgress.value.coerceAtMost(0.5f)
    val imageHeight = height * imageHeightFraction
    val scaledImageHeight = imageHeight * imageScale
    val imageTopPadding = imageHeight * (1 - imageScale)
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppTheme.specs.padding)
    ) {
        CoverImage(
            data = imageData,
            shape = RectangleShape,
            elevation = 10.dp,
            icon = icon ?: rememberVectorPainter(Icons.Default.MusicNote),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = imageTopPadding)
                .height(scaledImageHeight)
                .alpha(imageAlpha)
        )
        Text(
            text = title,
            style = titleStyle,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            modifier = titleModifier.align(Alignment.Start)
        )
    }
}
