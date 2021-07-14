/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.audios

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextOverflow
import com.google.accompanist.coil.rememberCoilPainter
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.material.shimmer
import tm.alashow.datmusic.domain.entities.Audio
import tm.alashow.ui.components.CoverImage
import tm.alashow.ui.theme.AppTheme

object AudiosDefaults

@Composable
fun AudioRow(
    audio: Audio,
    isPlaceholder: Boolean = false,
    onClick: ((Audio) -> Unit)? = null,
) {
    var menuVisible by remember { mutableStateOf(false) }
    val contentScaleOnMenuVisible = animateFloatAsState((if (menuVisible) 0.97f else 1f))

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .clickable { if (onClick != null) onClick(audio) else menuVisible = true }
            .fillMaxWidth()
            .padding(AppTheme.specs.inputPaddings)
    ) {
        val loadingModifier = Modifier.placeholder(
            visible = isPlaceholder,
            highlight = PlaceholderHighlight.shimmer(),
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(AppTheme.specs.padding),
            modifier = Modifier
                .weight(19f)
                .graphicsLayer {
                    scaleX *= contentScaleOnMenuVisible.value
                    scaleY *= contentScaleOnMenuVisible.value
                }
        ) {
            val image = rememberCoilPainter(audio.coverUrlSmall, fadeIn = true)
            CoverImage(image) { modifier ->
                Image(
                    painter = image,
                    contentDescription = null,
                    modifier = modifier.composed { loadingModifier }
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(AppTheme.specs.paddingSmall)) {
                Text(
                    audio.title,
                    style = MaterialTheme.typography.body1,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = loadingModifier
                )
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    Text(
                        audio.artist,
                        style = MaterialTheme.typography.body2,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = loadingModifier
                    )
                }
            }
        }

        if (!isPlaceholder) {
            val actionHandler = AudioActionHandler()

            AudioDropdownMenu(
                expanded = menuVisible,
                onExpandedChange = { menuVisible = it },
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .weight(1f),
                onDropdownSelect = {
                    actionHandler(AudioItemAction.from(it, audio))
                },
            )
        }
    }
}
