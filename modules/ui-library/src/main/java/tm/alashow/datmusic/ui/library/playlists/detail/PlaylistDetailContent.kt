/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.library.playlists.detail

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import tm.alashow.common.compose.LocalPlaybackConnection
import tm.alashow.datmusic.domain.entities.Audio
import tm.alashow.datmusic.domain.entities.PlaylistWithAudios
import tm.alashow.datmusic.ui.audios.AudioRow
import tm.alashow.datmusic.ui.detail.MediaDetailContent
import tm.alashow.domain.models.Async
import tm.alashow.domain.models.Loading
import tm.alashow.domain.models.Success

class PlaylistDetailContent : MediaDetailContent<PlaylistWithAudios>() {
    override fun invoke(list: LazyListScope, details: Async<PlaylistWithAudios>, detailsLoading: Boolean): Boolean {
        val playlistAudios = when (details) {
            is Success -> details().audios
            is Loading -> (1..5).map { Audio() }
            else -> emptyList()
        }

        if (playlistAudios.isNotEmpty()) {
            list.itemsIndexed(playlistAudios, key = { i, a -> a.id + i }) { index, audio ->
                val playbackConnection = LocalPlaybackConnection.current
                AudioRow(
                    audio = audio,
                    isPlaceholder = detailsLoading,
                    onPlayAudio = {
                        if (details is Success)
                            playbackConnection.playPlaylist(details(), index)
                    }
                )
            }
        }
        return playlistAudios.isEmpty()
    }
}
