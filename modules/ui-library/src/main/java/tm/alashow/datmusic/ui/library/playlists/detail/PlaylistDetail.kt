/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.library.playlists.detail

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
    val viewState by rememberFlowWithLifecycle(viewModel.state)
    var filterVisible by rememberSaveable { mutableStateOf(false) }
    val playlistId = viewState.playlist?.id
    MediaDetail(
        viewState = viewState,
        scrollbarsEnabled = viewState.isLoaded && !viewState.isEmpty,
        titleRes = R.string.playlist_title,
        onFailRetry = viewModel::refresh,
        onEmptyRetry = viewModel::addSongs,
        onTitleClick = {
            if (playlistId != null)
                navigator.navigate(EditPlaylistScreen.buildRoute(playlistId))
        },
        isHeaderVisible = !filterVisible,
        mediaDetailContent = PlaylistDetailContent(
            onRemoveFromPlaylist = viewModel::removePlaylistItem,
            onPlayAudio = viewModel::onPlayAudio
        ),
        mediaDetailTopBar = PlaylistDetailTopBar(
            filterVisible = filterVisible,
            setFilterVisible = { filterVisible = it },
            searchQuery = viewState.params.query,
            hasSortingOption = viewState.params.hasSortingOption,
            sortOptions = viewState.params.sortOptions,
            sortOption = viewState.params.sortOption,
            onSearchQueryChange = viewModel::onSearchQueryChange,
            onClearFilter = viewModel::onClearFilter,
            onSortOptionSelect = viewModel::onSortOptionSelect,
        ),
        mediaDetailEmpty = PlaylistDetailEmpty(),
        mediaDetailFail = PlaylistDetailFail(),
        extraHeaderContent = {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                PlaylistHeaderSubtitle(viewState)
                ShuffleAdaptiveButton(
                    visible = !viewState.isLoading && !viewState.isEmpty,
                    playbackConnection = playbackConnection,
                ) {
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
        val countDuration = viewState.audiosCountDuration
        if (countDuration != null)
            Text(countDuration.localize(resources), style = MaterialTheme.typography.subtitle2)
        else Spacer(Modifier)
    } else Spacer(Modifier)
}
