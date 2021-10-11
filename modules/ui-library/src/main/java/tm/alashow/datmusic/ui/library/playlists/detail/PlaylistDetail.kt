/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.library.playlists.detail

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import tm.alashow.common.compose.LocalPlaybackConnection
import tm.alashow.common.compose.rememberFlowWithLifecycle
import tm.alashow.datmusic.domain.entities.Audio
import tm.alashow.datmusic.domain.entities.PlaylistWithAudios
import tm.alashow.datmusic.ui.audios.AudioRow
import tm.alashow.datmusic.ui.detail.MediaDetail
import tm.alashow.datmusic.ui.library.R
import tm.alashow.datmusic.ui.utils.AudiosCountDurationTextCreator.localize
import tm.alashow.domain.models.Async
import tm.alashow.domain.models.Loading
import tm.alashow.domain.models.Success
import tm.alashow.navigation.LocalNavigator
import tm.alashow.navigation.Navigator
import tm.alashow.navigation.screens.EditPlaylistScreen
import tm.alashow.ui.components.EmptyErrorBox

@Composable
fun PlaylistDetail() {
    PlaylistDetail(viewModel = hiltViewModel())
}

@Composable
private fun PlaylistDetail(viewModel: PlaylistDetailViewModel, navigator: Navigator = LocalNavigator.current) {
    val viewState by rememberFlowWithLifecycle(viewModel.state).collectAsState(initial = PlaylistDetailViewState.Empty)

    MediaDetail(
        viewState = viewState,
        titleRes = R.string.playlist_title,
        onFailRetry = viewModel::refresh,
        onEmptyRetry = viewModel::addSongs,
        onTitleClick = {
            viewState.playlist?.id?.let { playlistId ->
                navigator.navigate(EditPlaylistScreen.buildRoute(playlistId))
            }
        },
        mediaDetails = { a, b ->
            playlistDetails(a, b)
        },
        mediaDetailsEmpty = { a, b, c ->
            playlistDetailsEmpty(a, b, c)
        },
        extraHeaderContent = {
            PlaylistHeaderSubtitle(viewState)
        }
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

private fun LazyListScope.playlistDetails(
    details: Async<PlaylistWithAudios>,
    detailsLoading: Boolean
): Boolean {
    val playlistAudios = when (details) {
        is Success -> details().audios
        is Loading -> (1..5).map { Audio() }
        else -> emptyList()
    }

    if (playlistAudios.isNotEmpty()) {
        itemsIndexed(playlistAudios, key = { i, a -> a.id + i }) { index, audio ->
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

private fun LazyListScope.playlistDetailsEmpty(
    details: Async<PlaylistWithAudios>,
    detailsEmpty: Boolean,
    onEmptyRetry: () -> Unit,
) {
    if (details is Success && detailsEmpty) {
        item {
            EmptyErrorBox(
                onRetryClick = onEmptyRetry,
                message = stringResource(R.string.playlist_empty),
                retryLabel = stringResource(R.string.playlist_empty_addSongs),
                modifier = Modifier.fillParentMaxHeight(0.5f)
            )
        }
    }
}
