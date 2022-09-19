/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.library.playlists.detail

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlaylistRemove
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import me.saket.swipe.SwipeAction
import tm.alashow.datmusic.domain.entities.PlaylistAudio
import tm.alashow.datmusic.domain.entities.PlaylistItem
import tm.alashow.datmusic.domain.entities.PlaylistItems
import tm.alashow.datmusic.ui.audios.AUDIO_SWIPE_ACTION_WEIGHT_MEDIUM
import tm.alashow.datmusic.ui.audios.AudioRow
import tm.alashow.datmusic.ui.detail.MediaDetailContent
import tm.alashow.datmusic.ui.library.R
import tm.alashow.domain.models.Async
import tm.alashow.domain.models.Loading
import tm.alashow.domain.models.Success
import tm.alashow.ui.contentColor
import tm.alashow.ui.theme.AppTheme
import tm.alashow.ui.theme.Red

private val RemoveFromPlaylist = R.string.playlist_audio_removeFromPlaylist

internal class PlaylistDetailContent(
    private val onPlayAudio: (PlaylistItem) -> Unit,
    private val onRemoveFromPlaylist: (PlaylistItem) -> Unit,
) : MediaDetailContent<PlaylistItems>() {

    @OptIn(ExperimentalFoundationApi::class)
    override fun invoke(list: LazyListScope, details: Async<PlaylistItems>, detailsLoading: Boolean): Boolean {
        val playlistAudios = when (details) {
            is Success -> details()
            is Loading -> (1..10).map { PlaylistItem(PlaylistAudio(it.toLong())) }
            else -> emptyList()
        }

        if (playlistAudios.isNotEmpty()) {
            list.itemsIndexed(playlistAudios, key = { _, it -> it.playlistAudio.id }) { index, item ->
                AudioRow(
                    audio = item.audio,
                    audioIndex = index,
                    isPlaceholder = detailsLoading,
                    onPlayAudio = {
                        if (details is Success)
                            onPlayAudio(item)
                    },
                    extraActionLabels = listOf(RemoveFromPlaylist),
                    onExtraAction = { onRemoveFromPlaylist(item) },
                    hasAddToPlaylistSwipeAction = false,
                    extraEndSwipeActions = listOf(
                        removeAudioFromPlaylistSwipeAction(
                            onRemoveFromPlaylist = {
                                onRemoveFromPlaylist(item)
                            }
                        )
                    ),
                    modifier = Modifier.animateItemPlacement()
                )
            }
        }
        return playlistAudios.isEmpty()
    }
}

@Composable
private fun removeAudioFromPlaylistSwipeAction(
    onRemoveFromPlaylist: () -> Unit,
    backgroundColor: Color = Red,
) = SwipeAction(
    background = backgroundColor,
    weight = AUDIO_SWIPE_ACTION_WEIGHT_MEDIUM,
    icon = {
        Icon(
            modifier = Modifier.padding(AppTheme.specs.padding),
            painter = rememberVectorPainter(Icons.Default.PlaylistRemove),
            tint = backgroundColor.contentColor(),
            contentDescription = null
        )
    },
    onSwipe = onRemoveFromPlaylist,
    isUndo = false,
)
