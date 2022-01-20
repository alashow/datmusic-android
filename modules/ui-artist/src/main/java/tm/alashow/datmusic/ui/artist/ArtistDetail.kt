/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.artist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.hilt.navigation.compose.hiltViewModel
import tm.alashow.common.compose.LocalPlaybackConnection
import tm.alashow.common.compose.rememberFlowWithLifecycle
import tm.alashow.datmusic.playback.PlaybackConnection
import tm.alashow.datmusic.playback.models.MEDIA_ID_INDEX_SHUFFLED
import tm.alashow.datmusic.ui.components.ShuffleAdaptiveButton
import tm.alashow.datmusic.ui.detail.MediaDetail

@Composable
fun ArtistDetail() {
    ArtistDetail(viewModel = hiltViewModel())
}

@Composable
private fun ArtistDetail(
    viewModel: ArtistDetailViewModel,
    playbackConnection: PlaybackConnection = LocalPlaybackConnection.current,
) {
    val viewState by rememberFlowWithLifecycle(viewModel.state)
    val artistId = viewState.artist?.id
    MediaDetail(
        viewState = viewState,
        titleRes = R.string.artists_detail_title,
        onFailRetry = viewModel::refresh,
        onEmptyRetry = viewModel::refresh,
        mediaDetailContent = ArtistDetailContent(),
        headerCoverIcon = rememberVectorPainter(Icons.Default.Person),
        extraHeaderContent = {
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                ShuffleAdaptiveButton(
                    visible = !viewState.isLoading && !viewState.isEmpty,
                    playbackConnection
                ) {
                    if (artistId != null)
                        playbackConnection.playArtist(artistId, MEDIA_ID_INDEX_SHUFFLED)
                }
            }
        },
    )
}
