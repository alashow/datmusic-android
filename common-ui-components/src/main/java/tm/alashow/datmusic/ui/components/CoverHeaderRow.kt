/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.accompanist.coil.rememberCoilPainter
import tm.alashow.ui.components.ImageWithPlaceholder
import tm.alashow.ui.theme.AppTheme
import tm.alashow.ui.theme.textShadow

object CoverHeaderDefaults {
    val height = 300.dp
}

@Composable
fun CoverHeaderRow(
    title: String,
    imageRequest: Any? = null,
    height: Dp = CoverHeaderDefaults.height,
    titleStyle: TextStyle = MaterialTheme.typography.h4.copy(color = Color.White, shadow = textShadow()),
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

        Text(
            text = title,
            style = titleStyle,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(AppTheme.specs.padding)
        )
    }
}
