/*
 * Copyright (C) 2022, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.downloads.audio

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Remove
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import me.saket.swipe.SwipeAction
import me.saket.swipe.SwipeableActionsBox
import tm.alashow.datmusic.domain.entities.AudioDownloadItem
import tm.alashow.datmusic.downloader.isComplete
import tm.alashow.datmusic.downloader.isIncomplete
import tm.alashow.datmusic.ui.audios.addAudioToPlaylistSwipeAction
import tm.alashow.datmusic.ui.audios.addAudioToQueueSwipeAction
import tm.alashow.ui.DEFAULT_SWIPE_ACTION_THRESHOLD
import tm.alashow.ui.contentColor
import tm.alashow.ui.theme.AppTheme
import tm.alashow.ui.theme.Blue
import tm.alashow.ui.theme.Orange
import tm.alashow.ui.theme.Red

@Composable
fun AudioDownloadBoxWithSwipeActions(
    audioDownloadItem: AudioDownloadItem,
    onAddToPlaylist: () -> Unit,
    content: @Composable BoxScope.() -> Unit,
) {
    SwipeableActionsBox(
        swipeThreshold = DEFAULT_SWIPE_ACTION_THRESHOLD,
        content = { content() },
        startActions = listOf(addAudioToQueueSwipeAction(audioDownloadItem.audio)),
        endActions = buildList {
            add(addAudioToPlaylistSwipeAction(onAddToPlaylist))
            if (audioDownloadItem.downloadInfo.isIncomplete()) {
                add(deleteAudioDownloadSwipeAction(audioDownloadItem))
            }
            if (audioDownloadItem.downloadInfo.isComplete()) {
                add(openAudioDownloadSwipeAction(audioDownloadItem))
                add(removeAudioDownloadSwipeAction(audioDownloadItem))
            }
        },
    )
}

@Composable
fun openAudioDownloadSwipeAction(
    audioDownloadItem: AudioDownloadItem,
    weight: Double = 1.0,
    backgroundColor: Color = Blue,
    actionHandler: AudioDownloadItemActionHandler = LocalAudioDownloadItemActionHandler.current,
) = SwipeAction(
    background = backgroundColor,
    weight = weight,
    icon = {
        Icon(
            modifier = Modifier.padding(AppTheme.specs.padding),
            painter = rememberVectorPainter(Icons.Default.OpenInNew),
            tint = backgroundColor.contentColor(),
            contentDescription = null
        )
    },
    onSwipe = {
        actionHandler(AudioDownloadItemAction.Open(audioDownloadItem))
    },
    isUndo = false,
)

@Composable
fun removeAudioDownloadSwipeAction(
    audioDownloadItem: AudioDownloadItem,
    weight: Double = 1.0,
    backgroundColor: Color = Orange,
    actionHandler: AudioDownloadItemActionHandler = LocalAudioDownloadItemActionHandler.current,
) = SwipeAction(
    background = backgroundColor,
    icon = {
        Icon(
            modifier = Modifier.padding(AppTheme.specs.padding),
            painter = rememberVectorPainter(Icons.Default.Remove),
            tint = backgroundColor.contentColor(),
            contentDescription = null
        )
    },
    onSwipe = {
        actionHandler(AudioDownloadItemAction.Remove(audioDownloadItem))
    },
    weight = weight,
    isUndo = false,
)

@Composable
fun deleteAudioDownloadSwipeAction(
    audioDownloadItem: AudioDownloadItem,
    weight: Double = 1.0,
    backgroundColor: Color = Red,
    actionHandler: AudioDownloadItemActionHandler = LocalAudioDownloadItemActionHandler.current
) = SwipeAction(
    background = backgroundColor,
    weight = weight,
    icon = {
        Icon(
            modifier = Modifier.padding(AppTheme.specs.padding),
            painter = rememberVectorPainter(Icons.Default.Delete),
            tint = backgroundColor.contentColor(),
            contentDescription = null
        )
    },
    onSwipe = {
        actionHandler(AudioDownloadItemAction.Delete(audioDownloadItem))
    },
    isUndo = false,
)
