/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.accompanist.coil.rememberCoilPainter
import tm.alashow.ui.components.ImageWithPlaceholder
import tm.alashow.ui.theme.AppTheme
import tm.alashow.ui.theme.textShadow

@Composable
fun CoverHeaderRow(
    title: String,
    imageRequest: Any? = null,
    height: Dp = 300.dp,
    titleStyle: TextStyle = MaterialTheme.typography.h4.copy(color = Color.White, shadow = textShadow()),
    imageMask: Brush = Brush.verticalGradient(
        colors = listOf(
            Color.Transparent,
            MaterialTheme.colors.primary.copy(alpha = 0.05f),
            MaterialTheme.colors.primary.copy(alpha = 0.1f)
        )
    ),
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        val painter = rememberCoilPainter(imageRequest, fadeIn = true)
        ImageWithPlaceholder(
            painter = painter,
            modifier = Modifier.height(height),
        ) {
            Image(
                painter = painter,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = it.fillMaxWidth()
            )
        }
        Box(
            Modifier
                .height(height)
                .fillMaxWidth()
                .background(brush = imageMask)
        )

        Text(
            text = title,
            style = titleStyle,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(AppTheme.specs.padding)
        )
    }
}
