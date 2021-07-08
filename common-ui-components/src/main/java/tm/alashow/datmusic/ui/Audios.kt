/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.google.accompanist.coil.rememberCoilPainter
import tm.alashow.datmusic.domain.entities.Audio
import tm.alashow.ui.components.CoverImage
import tm.alashow.ui.theme.AppTheme

object AudiosDefaults

@Composable
fun AudioRow(
    audio: Audio,
    onClick: (Audio) -> Unit = {},
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(AppTheme.specs.padding),
        modifier = Modifier
            .clickable { onClick(audio) }
            .fillMaxWidth()
            .padding(AppTheme.specs.inputPaddings)
    ) {
        val image = rememberCoilPainter(audio.coverUrlSmall, fadeIn = true)
        CoverImage(image) { modifier ->
            Image(
                painter = image,
                contentDescription = null,
                modifier = modifier
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(AppTheme.specs.paddingSmall)) {
            Text(
                audio.title,
                style = MaterialTheme.typography.body1,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                Text(
                    audio.artist,
                    style = MaterialTheme.typography.body2,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
