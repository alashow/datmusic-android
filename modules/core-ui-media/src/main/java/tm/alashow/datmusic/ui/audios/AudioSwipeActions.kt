/*
 * Copyright (C) 2022, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.audios

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import me.saket.swipe.SwipeAction
import me.saket.swipe.SwipeableActionsBox
import tm.alashow.datmusic.domain.entities.Audio
import tm.alashow.ui.DEFAULT_SWIPE_ACTION_THRESHOLD
import tm.alashow.ui.contentColor
import tm.alashow.ui.theme.AppTheme
import tm.alashow.ui.theme.Blue
import tm.alashow.ui.theme.LocalAdaptiveColor

const val AUDIO_SWIPE_ACTION_WEIGHT_NORMAL = 1.0
const val AUDIO_SWIPE_ACTION_WEIGHT_MEDIUM = 1.4

@Composable
fun AudioBoxWithSwipeActions(
    audio: Audio,
    onAddToPlaylist: () -> Unit,
    modifier: Modifier = Modifier,
    hasAddToPlaylistSwipeAction: Boolean = true,
    hasDownloadSwipeAction: Boolean = true,
    extraEndActions: List<SwipeAction> = emptyList(),
    content: @Composable BoxScope.() -> Unit,
) {
    SwipeableActionsBox(
        swipeThreshold = DEFAULT_SWIPE_ACTION_THRESHOLD,
        startActions = listOf(addAudioToQueueSwipeAction(audio)),
        endActions = buildList {
            if (hasAddToPlaylistSwipeAction) add(addAudioToPlaylistSwipeAction(onAddToPlaylist))
            if (hasDownloadSwipeAction) add(audioDownloadPlaylistSwipeAction(audio))
            addAll(extraEndActions)
        },
        modifier = modifier,
        content = { content() },
    )
}

@Composable
fun addAudioToQueueSwipeAction(
    audio: Audio,
    backgroundColor: Color = LocalAdaptiveColor.current.color,
    iconColor: Color = LocalAdaptiveColor.current.contentColor,
    actionHandler: AudioActionHandler = LocalAudioActionHandler.current,
) = SwipeAction(
    background = backgroundColor,
    icon = {
        Icon(
            modifier = Modifier.padding(AppTheme.specs.padding),
            painter = rememberVectorPainter(Icons.Default.QueueMusic),
            tint = iconColor,
            contentDescription = null
        )
    },
    onSwipe = { actionHandler(AudioItemAction.PlayNext(audio)) },
    isUndo = false,
)

@Composable
fun addAudioToPlaylistSwipeAction(
    onAddToPlaylist: () -> Unit,
    weight: Double = AUDIO_SWIPE_ACTION_WEIGHT_NORMAL,
    backgroundColor: Color = LocalAdaptiveColor.current.color,
    iconColor: Color = LocalAdaptiveColor.current.contentColor,
) = SwipeAction(
    background = backgroundColor,
    weight = weight,
    icon = {
        Icon(
            modifier = Modifier.padding(AppTheme.specs.padding),
            painter = rememberVectorPainter(Icons.Default.PlaylistAdd),
            tint = iconColor,
            contentDescription = null
        )
    },
    onSwipe = onAddToPlaylist,
    isUndo = false,
)

@Composable
fun audioDownloadPlaylistSwipeAction(
    audio: Audio,
    backgroundColor: Color = Blue,
    actionHandler: AudioActionHandler = LocalAudioActionHandler.current,
) = SwipeAction(
    background = backgroundColor,
    icon = {
        Icon(
            modifier = Modifier.padding(AppTheme.specs.padding),
            painter = rememberVectorPainter(Icons.Default.Download),
            tint = backgroundColor.contentColor(),
            contentDescription = null
        )
    },
    onSwipe = { actionHandler(AudioItemAction.Download(audio)) },
    weight = AUDIO_SWIPE_ACTION_WEIGHT_MEDIUM,
    isUndo = false,
)
