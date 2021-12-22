/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.library.playlists.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import tm.alashow.common.compose.LocalPlaybackConnection
import tm.alashow.common.compose.rememberFlowWithLifecycle
import tm.alashow.datmusic.playback.PlaybackConnection
import tm.alashow.datmusic.playback.models.MEDIA_ID_INDEX_SHUFFLED
import tm.alashow.datmusic.ui.components.ShuffleAdaptiveButton
import tm.alashow.datmusic.ui.detail.MediaDetail
import tm.alashow.datmusic.ui.library.R
import tm.alashow.datmusic.ui.utils.AudiosCountDurationTextCreator.localize
import tm.alashow.navigation.LocalNavigator
import tm.alashow.navigation.Navigator
import tm.alashow.navigation.screens.EditPlaylistScreen

@Composable
fun PlaylistDetail() {
    PlaylistDetail(viewModel = hiltViewModel())
}

@Composable
private fun PlaylistDetail(
    viewModel: PlaylistDetailViewModel,
    navigator: Navigator = LocalNavigator.current,
    playbackConnection: PlaybackConnection = LocalPlaybackConnection.current,
) {
    val viewState by rememberFlowWithLifecycle(viewModel.state).collectAsState(initial = PlaylistDetailViewState.Empty)
    val playlistId = viewState.playlist?.id
    MediaDetail(
        viewState = viewState,
        titleRes = R.string.playlist_title,
        onFailRetry = viewModel::refresh,
        onEmptyRetry = viewModel::addSongs,
        onTitleClick = {
            if (playlistId != null)
                navigator.navigate(EditPlaylistScreen.buildRoute(playlistId))
        },
        mediaDetailContent = PlaylistDetailContent.create(onRemoveFromPlaylist = viewModel::removePlaylistItem),
        mediaDetailEmpty = PlaylistDetailEmpty(),
        extraHeaderContent = {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                PlaylistHeaderSubtitle(viewState)
                if (viewState.isLoaded && !viewState.isEmpty)
                    ShuffleAdaptiveButton(playbackConnection) {
                        if (playlistId != null)
                            playbackConnection.playPlaylist(playlistId, MEDIA_ID_INDEX_SHUFFLED)
                    }
            }
        },
    )
}

@Composable
private fun PlaylistHeaderSubtitle(viewState: PlaylistDetailViewState) {
    if (!viewState.isEmpty) {
        val resources = LocalContext.current.resources
        viewState.audiosCountDuration?.let {
            Text(it.localize(resources), style = MaterialTheme.typography.subtitle2)
        }
    }
}
