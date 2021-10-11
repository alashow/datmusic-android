/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.album

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberImagePainter
import tm.alashow.base.imageloading.ImageLoading
import tm.alashow.base.util.extensions.interpunctize
import tm.alashow.base.util.extensions.orNA
import tm.alashow.common.compose.LocalPlaybackConnection
import tm.alashow.common.compose.rememberFlowWithLifecycle
import tm.alashow.datmusic.domain.entities.Album
import tm.alashow.datmusic.domain.entities.Audio
import tm.alashow.datmusic.domain.entities.CoverImageSize
import tm.alashow.datmusic.ui.audios.AudioRow
import tm.alashow.datmusic.ui.detail.MediaDetail
import tm.alashow.domain.models.Async
import tm.alashow.domain.models.Loading
import tm.alashow.domain.models.Success
import tm.alashow.ui.components.CoverImage
import tm.alashow.ui.theme.AppTheme

@Composable
fun AlbumDetail() {
    AlbumDetail(viewModel = hiltViewModel())
}

@Composable
private fun AlbumDetail(viewModel: AlbumDetailViewModel) {
    val viewState by rememberFlowWithLifecycle(viewModel.state).collectAsState(initial = AlbumDetailViewState.Empty)

    MediaDetail(
        viewState = viewState,
        titleRes = R.string.albums_detail_title,
        onFailRetry = viewModel::refresh,
        onEmptyRetry = viewModel::refresh,
        mediaDetails = { a, b ->
            albumAudios(viewState.album ?: Album(), a, b)
        },
        extraHeaderContent = {
            AlbumHeaderSubtitle(viewState)
        }
    )
}

@Composable
private fun AlbumHeaderSubtitle(viewState: AlbumDetailViewState) {

    val artist = viewState.album?.artists?.firstOrNull()
    val albumSubtitle = listOf(stringResource(R.string.albums_detail_title), viewState.album?.year?.toString()).interpunctize()

    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppTheme.specs.paddingSmall)) {
        val painter = rememberImagePainter(artist?.photo(CoverImageSize.SMALL), builder = ImageLoading.defaultConfig)
        CoverImage(painter, shape = CircleShape, size = 20.dp)
        Text(artist?.name.orNA(), style = MaterialTheme.typography.subtitle2)
    }
    Text(albumSubtitle, style = MaterialTheme.typography.caption)
}

private fun LazyListScope.albumAudios(
    album: Album,
    details: Async<List<Audio>>,
    detailsLoading: Boolean,
): Boolean {
    val albumAudios = when (details) {
        is Success -> details()
        is Loading -> (1..album.songCount).map { Audio() }
        else -> emptyList()
    }

    if (albumAudios.isNotEmpty()) {
        item {
            Text(
                stringResource(R.string.search_audios),
                style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppTheme.specs.inputPaddings)
            )
        }

        itemsIndexed(albumAudios, key = { i, a -> a.id + i }) { index, audio ->
            val playbackConnection = LocalPlaybackConnection.current
            AudioRow(
                audio = audio,
                isPlaceholder = detailsLoading,
                onPlayAudio = {
                    if (details is Success)
                        playbackConnection.playAlbum(album, index)
                }
            )
        }
    }
    return albumAudios.isEmpty()
}
