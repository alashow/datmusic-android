/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.album

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import tm.alashow.datmusic.domain.entities.Album
import tm.alashow.datmusic.domain.entities.Audio
import tm.alashow.datmusic.domain.entities.Audios
import tm.alashow.datmusic.ui.audios.AudioRow
import tm.alashow.datmusic.ui.detail.MediaDetailContent
import tm.alashow.datmusic.ui.playback.LocalPlaybackConnection
import tm.alashow.domain.models.Async
import tm.alashow.domain.models.Loading
import tm.alashow.domain.models.Success

internal class AlbumDetailContent(val album: Album) : MediaDetailContent<Audios>() {

    override fun invoke(list: LazyListScope, details: Async<Audios>, detailsLoading: Boolean): Boolean {
        val albumAudios = when (details) {
            is Success -> details()
            is Loading -> (1..album.songCount).map { Audio() }
            else -> emptyList()
        }

        if (albumAudios.isNotEmpty()) {
            list.itemsIndexed(albumAudios, key = { i, a -> a.id + i }) { index, audio ->
                val playbackConnection = LocalPlaybackConnection.current
                AudioRow(
                    audio = audio,
                    isPlaceholder = detailsLoading,
                    includeCover = false,
                    onPlayAudio = {
                        if (details is Success)
                            playbackConnection.playAlbum(album.id, index)
                    }
                )
            }
        }
        return albumAudios.isEmpty()
    }
}
