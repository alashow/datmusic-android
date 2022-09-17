/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.album

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay
import tm.alashow.base.util.extensions.interpunctize
import tm.alashow.base.util.extensions.orNA
import tm.alashow.common.compose.LocalIsPreviewMode
import tm.alashow.common.compose.previews.CombinedPreview
import tm.alashow.common.compose.rememberFlowWithLifecycle
import tm.alashow.datmusic.data.SampleData
import tm.alashow.datmusic.domain.entities.Album
import tm.alashow.datmusic.playback.PlaybackConnection
import tm.alashow.datmusic.playback.models.MEDIA_ID_INDEX_SHUFFLED
import tm.alashow.datmusic.ui.components.ShuffleAdaptiveButton
import tm.alashow.datmusic.ui.detail.MediaDetail
import tm.alashow.datmusic.ui.playback.LocalPlaybackConnection
import tm.alashow.datmusic.ui.previews.PreviewDatmusicCore
import tm.alashow.domain.models.Loading
import tm.alashow.domain.models.Success
import tm.alashow.ui.components.CoverImage
import tm.alashow.ui.simpleClickable
import tm.alashow.ui.theme.AppTheme

@Composable
fun AlbumDetailRoute(isPreviewMode: Boolean = LocalIsPreviewMode.current) {
    when {
        isPreviewMode -> AlbumDetailPreview()
        else -> AlbumDetail()
    }
}

@Composable
private fun AlbumDetail(
    viewModel: AlbumDetailViewModel = hiltViewModel(),
) {
    val viewState by rememberFlowWithLifecycle(viewModel.state)
    AlbumDetail(
        viewState = viewState,
        onRefresh = viewModel::refresh,
        onArtistClick = viewModel::goToArtist,
    )
}

@Composable
private fun AlbumDetail(
    viewState: AlbumDetailViewState,
    onRefresh: () -> Unit,
    onArtistClick: () -> Unit,
    playbackConnection: PlaybackConnection = LocalPlaybackConnection.current,
) {
    val albumId = viewState.album?.id
    MediaDetail(
        viewState = viewState,
        titleRes = R.string.albums_detail_title,
        onFailRetry = onRefresh,
        onEmptyRetry = onRefresh,
        mediaDetailContent = AlbumDetailContent(viewState.album ?: Album()),
        headerCoverIcon = rememberVectorPainter(Icons.Default.Album),
        extraHeaderContent = {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(AppTheme.specs.paddingSmall),
                    modifier = Modifier.weight(7f)
                ) {
                    AlbumHeaderSubtitle(
                        viewState = viewState,
                        onArtistClick = onArtistClick
                    )
                }
                ShuffleAdaptiveButton(
                    visible = !viewState.isLoading && !viewState.isEmpty,
                    modifier = Modifier.weight(1f),
                ) {
                    if (albumId != null)
                        playbackConnection.playAlbum(albumId, MEDIA_ID_INDEX_SHUFFLED)
                }
            }
        },
    )
}

@Composable
private fun AlbumHeaderSubtitle(viewState: AlbumDetailViewState, onArtistClick: () -> Unit) {
    val artist = viewState.album?.artists?.firstOrNull()
    val albumSubtitle = listOfNotNull(stringResource(R.string.albums_detail_title), viewState.album?.displayYear).interpunctize()
    val artistName = artist?.name.orNA()

    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppTheme.specs.paddingSmall)) {
        CoverImage(artist?.photo(), shape = CircleShape, size = 20.dp)
        Text(
            artistName, style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.simpleClickable(onClick = onArtistClick)
        )
    }
    Text(albumSubtitle, style = MaterialTheme.typography.bodySmall)
}

@CombinedPreview
@Composable
private fun AlbumDetailPreview() = PreviewDatmusicCore {
    var viewState by remember { mutableStateOf(AlbumDetailViewState.Empty) }
    LaunchedEffect(Unit) {
        delay(1000)
        val artist = SampleData.artist()
        val album = SampleData.album(mainArtist = artist)
        viewState = viewState.copy(album = album, albumDetails = Loading())
        delay(1000)
        viewState = viewState.copy(
            albumDetails = Success(
                SampleData.list {
                    audio().copy(artist = artist.name)
                }
            )
        )
    }
    AlbumDetail(
        viewState = viewState,
        onRefresh = {},
        onArtistClick = {},
    )
}
