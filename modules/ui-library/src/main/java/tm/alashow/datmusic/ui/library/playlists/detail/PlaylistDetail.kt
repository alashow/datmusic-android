/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.library.playlists.detail

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay
import tm.alashow.common.compose.LocalIsPreviewMode
import tm.alashow.common.compose.previews.CombinedPreview
import tm.alashow.common.compose.rememberFlowWithLifecycle
import tm.alashow.datmusic.data.SampleData
import tm.alashow.datmusic.data.observers.playlist.PlaylistItemSortOption
import tm.alashow.datmusic.domain.entities.PlaylistItem
import tm.alashow.datmusic.playback.PlaybackConnection
import tm.alashow.datmusic.playback.models.MEDIA_ID_INDEX_SHUFFLED
import tm.alashow.datmusic.ui.components.ShuffleAdaptiveButton
import tm.alashow.datmusic.ui.detail.MediaDetail
import tm.alashow.datmusic.ui.library.R
import tm.alashow.datmusic.ui.playback.LocalPlaybackConnection
import tm.alashow.datmusic.ui.previews.PreviewDatmusicCore
import tm.alashow.datmusic.ui.utils.AudiosCountDurationTextCreator.localize
import tm.alashow.domain.models.Loading
import tm.alashow.domain.models.Success
import tm.alashow.navigation.LocalNavigator
import tm.alashow.navigation.Navigator
import tm.alashow.navigation.screens.EditPlaylistScreen

@Composable
fun PlaylistDetailRoute(isPreviewMode: Boolean = LocalIsPreviewMode.current) {
    when {
        isPreviewMode -> PlaylistDetailPreview()
        else -> PlaylistDetail()
    }
}

@Composable
private fun PlaylistDetail(viewModel: PlaylistDetailViewModel = hiltViewModel()) {
    val viewState by rememberFlowWithLifecycle(viewModel.state)
    PlaylistDetail(
        viewState = viewState,
        onRefresh = viewModel::refresh,
        onAddSongs = viewModel::addSongs,
        onRemovePlaylistItem = viewModel::onRemovePlaylistItem,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onSortOptionSelect = viewModel::onSortOptionSelect,
        onClearFilter = viewModel::onClearFilter,
        onPlayPlaylistItem = viewModel::onPlayPlaylistItem,
    )
}

@Composable
private fun PlaylistDetail(
    viewState: PlaylistDetailViewState,
    onRefresh: () -> Unit,
    onAddSongs: () -> Unit,
    onRemovePlaylistItem: (PlaylistItem) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSortOptionSelect: (PlaylistItemSortOption) -> Unit,
    onClearFilter: () -> Unit,
    onPlayPlaylistItem: (PlaylistItem) -> Unit,
    navigator: Navigator = LocalNavigator.current,
    playbackConnection: PlaybackConnection = LocalPlaybackConnection.current,
) {
    var filterVisible by rememberSaveable { mutableStateOf(false) }
    val playlistId = viewState.playlist?.id
    MediaDetail(
        viewState = viewState,
        scrollbarsEnabled = viewState.isLoaded && !viewState.isEmpty,
        titleRes = R.string.playlist_title,
        onFailRetry = onRefresh,
        onEmptyRetry = onAddSongs,
        onTitleClick = {
            if (playlistId != null)
                navigator.navigate(EditPlaylistScreen.buildRoute(playlistId))
        },
        isHeaderVisible = !filterVisible,
        mediaDetailContent = PlaylistDetailContent(
            onPlayAudio = onPlayPlaylistItem,
            onRemoveFromPlaylist = onRemovePlaylistItem,
        ),
        mediaDetailTopBar = PlaylistDetailTopBar(
            filterVisible = filterVisible,
            setFilterVisible = { filterVisible = it },
            searchQuery = viewState.params.query,
            hasSortingOption = viewState.params.hasSortingOption,
            sortOptions = viewState.params.sortOptions,
            sortOption = viewState.params.sortOption,
            onSearchQueryChange = onSearchQueryChange,
            onSortOptionSelect = onSortOptionSelect,
            onClearFilter = onClearFilter,
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
            Text(countDuration.localize(resources), style = MaterialTheme.typography.titleSmall)
        else Spacer(Modifier)
    } else Spacer(Modifier)
}

@CombinedPreview
@Composable
private fun PlaylistDetailPreview() = PreviewDatmusicCore {
    val playlistItems = remember(Unit) { SampleData.list { playlistItem() } }
    var viewState by remember { mutableStateOf(PlaylistDetailViewState.Empty) }
    LaunchedEffect(Unit) {
        delay(1000)
        val playlist = SampleData.playlist()
        viewState = viewState.copy(playlist = playlist, playlistDetails = Loading())
        delay(1000)
        viewState = viewState.copy(playlistDetails = Success(playlistItems))
    }
    PlaylistDetail(
        viewState = viewState,
        onRefresh = {},
        onAddSongs = {},
        onRemovePlaylistItem = {
            viewState = viewState.copy(playlistDetails = Success(playlistItems - it))
        },
        onSearchQueryChange = { query ->
            viewState = viewState.copy(
                params = viewState.params.copy(query = query),
                playlistDetails = Success(playlistItems.filter { query in it.toString() })
            )
        },
        onSortOptionSelect = {
            val sortedPlaylistItems = it.comparator?.run { playlistItems.sortedWith(this) } ?: playlistItems
            viewState = viewState.copy(
                params = viewState.params.copy(sortOption = it),
                playlistDetails = Success(sortedPlaylistItems)
            )
        },
        onClearFilter = {},
        onPlayPlaylistItem = {},
    )
}
