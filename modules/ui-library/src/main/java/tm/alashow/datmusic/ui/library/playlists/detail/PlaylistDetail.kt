/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.library.playlists.detail

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.insets.statusBarsPadding
import com.google.accompanist.insets.ui.Scaffold
import kotlin.math.round
import tm.alashow.base.util.extensions.localizedMessage
import tm.alashow.base.util.extensions.localizedTitle
import tm.alashow.common.compose.LocalPlaybackConnection
import tm.alashow.common.compose.rememberFlowWithLifecycle
import tm.alashow.datmusic.domain.entities.Audio
import tm.alashow.datmusic.domain.entities.PlaylistWithAudios
import tm.alashow.datmusic.ui.audios.AudioRow
import tm.alashow.datmusic.ui.components.CoverHeaderDefaults
import tm.alashow.datmusic.ui.components.CoverHeaderRow
import tm.alashow.datmusic.ui.library.R
import tm.alashow.domain.models.Async
import tm.alashow.domain.models.Fail
import tm.alashow.domain.models.Incomplete
import tm.alashow.domain.models.Loading
import tm.alashow.domain.models.Success
import tm.alashow.navigation.LocalNavigator
import tm.alashow.navigation.Navigator
import tm.alashow.ui.OffsetNotifyingBox
import tm.alashow.ui.components.AppBarHeight
import tm.alashow.ui.components.CollapsingTopBar
import tm.alashow.ui.components.EmptyErrorBox
import tm.alashow.ui.components.ErrorBox
import tm.alashow.ui.components.fullScreenLoading
import tm.alashow.ui.theme.AppTheme

@Composable
fun PlaylistDetail(navigator: Navigator = LocalNavigator.current) {
    PlaylistDetail(viewModel = hiltViewModel()) {
        navigator.goBack()
    }
}

@Composable
private fun PlaylistDetail(viewModel: PlaylistDetailViewModel, onBackClick: () -> Unit = {}) {
    val viewState by rememberFlowWithLifecycle(viewModel.state).collectAsState(initial = PlaylistDetailViewState.Empty)
    val listState = rememberLazyListState()

    val headerHeight = CoverHeaderDefaults.height
    val headerOffsetProgress = remember { Animatable(0f) }

    OffsetNotifyingBox(headerHeight = headerHeight) { _, progress ->
        Scaffold(
            topBar = {
                LaunchedEffect(progress.value) {
                    headerOffsetProgress.animateTo(round(progress.value))
                }
                CollapsingTopBar(
                    title = stringResource(R.string.library_playlist),
                    collapsed = !viewState.isEmptyPlaylist && headerOffsetProgress.value == 0f,
                    onNavigationClick = onBackClick,
                )
            }
        ) { padding ->
            PlaylistDetailList(
                viewState = viewState,
                onFailRetry = viewModel::refresh,
                onEmptyRetry = viewModel::addSongs,
                padding = padding,
                listState = listState
            )
        }
    }
}

@Composable
private fun PlaylistDetailList(
    viewState: PlaylistDetailViewState,
    onFailRetry: () -> Unit,
    onEmptyRetry: () -> Unit,
    padding: PaddingValues = PaddingValues(),
    listState: LazyListState,
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = padding.calculateTopPadding() + padding.calculateBottomPadding())
    ) {
        val playlist = viewState.playlist
        if (playlist != null) {
            val details = viewState.playlistDetails
            val detailsLoading = details is Incomplete

            if (!viewState.isEmptyPlaylist) {
                var scrolledY = 0f
                var previousOffset = 0
                val parallax = 0.3f
                item {
                    CoverHeaderRow(
                        title = playlist.name,
                        imageRequest = "artist.largePhoto()",
                        modifier = Modifier.graphicsLayer {
                            scrolledY += listState.firstVisibleItemScrollOffset - previousOffset
                            translationY = scrolledY * parallax
                            previousOffset = listState.firstVisibleItemScrollOffset
                        }
                    )
                }
            }

            val artistAudios = playlistDetails(details, detailsLoading)

            playlistDetailsFail(details, onFailRetry)
            playlistDetailsEmpty(details, artistAudios.isEmpty(), onEmptyRetry)
        } else {
            fullScreenLoading()
        }
    }
}

private fun LazyListScope.playlistDetails(
    details: Async<PlaylistWithAudios>,
    detailsLoading: Boolean
): List<Audio> {
    val playlistAudios = when (details) {
        is Success -> details().audios
        is Loading -> (1..5).map { Audio() }
        else -> emptyList()
    }

    if (playlistAudios.isNotEmpty()) {
        item {
            Text(
                stringResource(R.string.search_audios), style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colors.background)
                    .padding(AppTheme.specs.inputPaddings)
            )
        }

        itemsIndexed(playlistAudios) { index, audio ->
            val playbackConnection = LocalPlaybackConnection.current
            AudioRow(
                audio = audio,
                isPlaceholder = detailsLoading,
                modifier = Modifier.background(MaterialTheme.colors.background),
                onPlayAudio = {
                    if (details is Success)
                        playbackConnection.playPlaylist(details(), index)
                }
            )
        }
    }
    return playlistAudios
}

private fun LazyListScope.playlistDetailsFail(
    details: Async<PlaylistWithAudios>,
    onFailRetry: () -> Unit,
) {
    if (details is Fail) {
        item {
            ErrorBox(
                title = stringResource(details.error.localizedTitle()),
                message = stringResource(details.error.localizedMessage()),
                onRetryClick = onFailRetry,
                modifier = Modifier.fillParentMaxHeight()
            )
        }
    }
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
                message = stringResource(R.string.library_playlist_empty),
                retryLabel = stringResource(R.string.library_playlist_empty_addSongs),
                modifier = Modifier
                    .fillParentMaxHeight()
                    .statusBarsPadding()
                    .padding(top = AppBarHeight)
            )
        }
    }
}
