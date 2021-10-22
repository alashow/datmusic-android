/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.album

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import tm.alashow.common.compose.LocalPlaybackConnection
import tm.alashow.datmusic.domain.entities.Album
import tm.alashow.datmusic.domain.entities.Audio
import tm.alashow.datmusic.domain.entities.Audios
import tm.alashow.datmusic.ui.audios.AudioRow
import tm.alashow.datmusic.ui.detail.MediaDetailContent
import tm.alashow.domain.models.Async
import tm.alashow.domain.models.Loading
import tm.alashow.domain.models.Success
import tm.alashow.ui.theme.AppTheme

class AlbumDetailContent(val album: Album) : MediaDetailContent<Audios>() {

    override fun invoke(list: LazyListScope, details: Async<Audios>, detailsLoading: Boolean): Boolean {
        val albumAudios = when (details) {
            is Success -> details()
            is Loading -> (1..album.songCount).map { Audio() }
            else -> emptyList()
        }

        if (albumAudios.isNotEmpty()) {
            list.item {
                Text(
                    stringResource(R.string.search_audios),
                    style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AppTheme.specs.inputPaddings)
                )
            }

            list.itemsIndexed(albumAudios, key = { i, a -> a.id + i }) { index, audio ->
                val playbackConnection = LocalPlaybackConnection.current
                AudioRow(
                    audio = audio,
                    audioIndex = index,
                    isPlaceholder = detailsLoading,
                    includeCover = false,
                    onPlayAudio = {
                        if (details is Success)
                            playbackConnection.playAlbum(album, index)
                    }
                )
            }
        }
        return albumAudios.isEmpty()
    }
}
