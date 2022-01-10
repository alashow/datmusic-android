/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.library.playlists.detail

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.Modifier
import tm.alashow.datmusic.domain.entities.*
import tm.alashow.datmusic.ui.audios.AudioRow
import tm.alashow.datmusic.ui.detail.MediaDetailContent
import tm.alashow.datmusic.ui.library.R
import tm.alashow.domain.models.Async
import tm.alashow.domain.models.Loading
import tm.alashow.domain.models.Success

private val RemoveFromPlaylist = R.string.playlist_audio_removeFromPlaylist

class PlaylistDetailContent(
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
                    modifier = Modifier.animateItemPlacement()
                )
            }
        }
        return playlistAudios.isEmpty()
    }
}
