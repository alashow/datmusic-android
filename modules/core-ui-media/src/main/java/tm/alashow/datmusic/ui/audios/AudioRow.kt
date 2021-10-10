/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.audios

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explicit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.google.accompanist.placeholder.material.placeholder
import tm.alashow.base.imageloading.ImageLoading
import tm.alashow.base.util.extensions.interpunctize
import tm.alashow.base.util.millisToDuration
import tm.alashow.datmusic.domain.entities.Audio
import tm.alashow.datmusic.ui.library.playlist.addTo.AddToPlaylistMenu
import tm.alashow.ui.components.CoverImage
import tm.alashow.ui.components.shimmer
import tm.alashow.ui.theme.AppTheme

object AudiosDefaults {
    val imageSize = 48.dp
    const val maxLines = 3
}

@Composable
fun AudioRow(
    audio: Audio,
    modifier: Modifier = Modifier,
    imageSize: Dp = AudiosDefaults.imageSize,
    isPlaceholder: Boolean = false,
    onClick: ((Audio) -> Unit)? = null,
    onPlayAudio: ((Audio) -> Unit)? = null,
    actionHandler: AudioActionHandler = LocalAudioActionHandler.current
) {
    var menuVisible by remember { mutableStateOf(false) }
    val contentScaleOnMenuVisible = animateFloatAsState((if (menuVisible) 0.97f else 1f))

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clickable {
                if (!isPlaceholder)
                    if (onClick != null) onClick(audio)
                    else menuVisible = true
            }
            .fillMaxWidth()
            .padding(AppTheme.specs.inputPaddings)
    ) {
        AudioRowItem(
            audio = audio,
            isPlaceholder = isPlaceholder,
            imageSize = imageSize,
            onCoverClick = {
                if (onPlayAudio != null) onPlayAudio(audio)
                else actionHandler(AudioItemAction.Play(audio))
            },
            modifier = Modifier
                .weight(19f)
                .graphicsLayer {
                    scaleX *= contentScaleOnMenuVisible.value
                    scaleY *= contentScaleOnMenuVisible.value
                }
        )

        if (!isPlaceholder) {
            val (addToPlaylistVisible, setAddToPlaylistVisible) = remember { mutableStateOf(false) }
            AddToPlaylistMenu(audio, addToPlaylistVisible, setAddToPlaylistVisible)
            AudioDropdownMenu(
                expanded = menuVisible,
                onExpandedChange = { menuVisible = it },
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .weight(1f),
                onDropdownSelect = {
                    val action = AudioItemAction.from(it, audio)
                    when {
                        action is AudioItemAction.Play && onPlayAudio != null -> onPlayAudio(audio)
                        action is AudioItemAction.AddToPlaylist -> setAddToPlaylistVisible(true)
                        else -> actionHandler(action)
                    }
                },
            )
        }
    }
}

@Composable
fun AudioRowItem(
    audio: Audio,
    modifier: Modifier = Modifier,
    imageSize: Dp = AudiosDefaults.imageSize,
    onCoverClick: (Audio) -> Unit = {},
    isPlaceholder: Boolean = false,
    maxLines: Int = AudiosDefaults.maxLines,
) {
    val loadingModifier = Modifier.placeholder(
        visible = isPlaceholder,
        highlight = shimmer(),
    )
    Row(
        horizontalArrangement = Arrangement.spacedBy(AppTheme.specs.padding),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        val image = rememberImagePainter(audio.coverUrlSmall ?: audio.coverUrl, builder = ImageLoading.defaultConfig)
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
                        onClick = { onCoverClick(audio) },
                    )
                    .then(loadingModifier)
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(AppTheme.specs.paddingTiny)) {
            Text(
                audio.title,
                style = MaterialTheme.typography.body2.copy(fontSize = 15.sp),
                maxLines = maxLines,
                overflow = TextOverflow.Ellipsis,
                modifier = loadingModifier
            )
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AppTheme.specs.paddingTiny),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (audio.explicit)
                        Icon(
                            painter = rememberVectorPainter(Icons.Filled.Explicit),
                            contentDescription = null,
                            modifier = Modifier
                                .size(18.dp)
                                .alignByBaseline(),
                            tint = MaterialTheme.colors.onBackground.copy(alpha = ContentAlpha.medium),
                        )
                    val artistAndDuration = listOf(audio.artist, audio.durationMillis().millisToDuration()).interpunctize()
                    Text(
                        artistAndDuration,
                        style = MaterialTheme.typography.subtitle2.copy(fontSize = 14.sp),
                        maxLines = maxLines,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .alignByBaseline()
                            .then(loadingModifier)
                    )
                }
            }
        }
    }
}
