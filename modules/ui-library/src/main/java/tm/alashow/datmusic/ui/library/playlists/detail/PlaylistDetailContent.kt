/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.library.playlists.detail

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import tm.alashow.common.compose.LocalPlaybackConnection
import tm.alashow.datmusic.domain.entities.AudioOfPlaylist
import tm.alashow.datmusic.domain.entities.AudiosOfPlaylist
import tm.alashow.datmusic.domain.entities.PlaylistAudio
import tm.alashow.datmusic.domain.entities.playlistId
import tm.alashow.datmusic.playback.PlaybackConnection
import tm.alashow.datmusic.ui.audios.AudioActionHandler
import tm.alashow.datmusic.ui.audios.AudioRow
import tm.alashow.datmusic.ui.audios.LocalAudioActionHandler
import tm.alashow.datmusic.ui.detail.MediaDetailContent
import tm.alashow.datmusic.ui.library.R
import tm.alashow.domain.models.Async
import tm.alashow.domain.models.Loading
import tm.alashow.domain.models.Success

private val RemoveFromPlaylist = R.string.playlist_audio_removeFromPlaylist

class PlaylistDetailContent(
    private val onRemoveFromPlaylist: (AudioOfPlaylist) -> Unit,
    private val playbackConnection: PlaybackConnection,
    private val audioActionHandler: AudioActionHandler
) : MediaDetailContent<AudiosOfPlaylist>() {

    companion object {
        @Composable
        fun create(
            onRemoveFromPlaylist: (AudioOfPlaylist) -> Unit,
            playbackConnection: PlaybackConnection = LocalPlaybackConnection.current,
            audioActionHandler: AudioActionHandler = LocalAudioActionHandler.current,
        ) = PlaylistDetailContent(onRemoveFromPlaylist, playbackConnection, audioActionHandler)
    }

    override fun invoke(list: LazyListScope, details: Async<AudiosOfPlaylist>, detailsLoading: Boolean): Boolean {
        val playlistAudios = when (details) {
            is Success -> details()
            is Loading -> (1..5).map { AudioOfPlaylist(playlistAudio = PlaylistAudio(id = it.toLong())) }
            else -> emptyList()
        }

        if (playlistAudios.isNotEmpty()) {
            list.itemsIndexed(playlistAudios, key = { i, it -> it.playlistAudio.id }) { index, audioOfPlaylist ->
                AudioRow(
                    audio = audioOfPlaylist.audio,
                    isPlaceholder = detailsLoading,
                    playOnClick = true,
                    onPlayAudio = {
                        if (details is Success) playbackConnection.playPlaylist(details().playlistId(), index)
                    },
                    extraActionLabels = listOf(RemoveFromPlaylist),
                    actionHandler = {
                        it.handleExtraAction(RemoveFromPlaylist, audioActionHandler) {
                            onRemoveFromPlaylist(audioOfPlaylist)
                        }
                    }
                )
            }
        }
        return playlistAudios.isEmpty()
    }
}
