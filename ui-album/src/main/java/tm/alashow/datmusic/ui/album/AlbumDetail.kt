/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.album

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.insets.ui.Scaffold
import kotlin.math.round
import kotlinx.coroutines.launch
import tm.alashow.base.util.extensions.localizedMessage
import tm.alashow.base.util.extensions.localizedTitle
import tm.alashow.common.compose.rememberFlowWithLifecycle
import tm.alashow.datmusic.domain.entities.Album
import tm.alashow.datmusic.domain.entities.Audio
import tm.alashow.datmusic.ui.audios.AudioRow
import tm.alashow.datmusic.ui.components.CoverHeaderDefaults
import tm.alashow.datmusic.ui.components.CoverHeaderRow
import tm.alashow.datmusic.ui.playback.LocalPlaybackConnection
import tm.alashow.domain.models.Async
import tm.alashow.domain.models.Fail
import tm.alashow.domain.models.Incomplete
import tm.alashow.domain.models.Loading
import tm.alashow.domain.models.Success
import tm.alashow.navigation.LocalNavigator
import tm.alashow.navigation.Navigator
import tm.alashow.ui.OffsetNotifyingBox
import tm.alashow.ui.components.CollapsingTopBar
import tm.alashow.ui.components.EmptyErrorBox
import tm.alashow.ui.components.ErrorBox
import tm.alashow.ui.components.FullScreenLoading
import tm.alashow.ui.theme.AppTheme

@Composable
fun AlbumDetail(navigator: Navigator = LocalNavigator.current) {
    AlbumDetail(viewModel = hiltViewModel()) {
        navigator.back()
    }
}

@Composable
private fun AlbumDetail(viewModel: AlbumDetailViewModel, onBackClick: () -> Unit = {}) {
    val viewState by rememberFlowWithLifecycle(viewModel.state).collectAsState(initial = AlbumDetailViewState.Empty)
    val listState = rememberLazyListState()

    val headerHeight = CoverHeaderDefaults.height
    val headerVisibilityProgress = Animatable(0f)

    OffsetNotifyingBox(headerHeight = headerHeight) { _, progress ->
        Scaffold(
            topBar = {
                LaunchedEffect(progress.value) {
                    headerVisibilityProgress.animateTo(round(progress.value))
                }
                CollapsingTopBar(
                    title = stringResource(R.string.albums_detail_title),
                    collapsed = headerVisibilityProgress.value == 0f,
                    onNavigationClick = onBackClick,
                )
            }
        ) { padding ->
            AlbumDetailList(viewState, viewModel::refresh, padding, listState)
        }
    }
}

@Composable
private fun AlbumDetailList(
    viewState: AlbumDetailViewState,
    onRetry: () -> Unit,
    padding: PaddingValues = PaddingValues(),
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints {
        LazyColumn(
            state = listState,
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = padding.calculateTopPadding() + padding.calculateBottomPadding())
        ) {
            val album = viewState.album
            if (album != null) {
                item {
                    var scrolledY = 0f
                    var previousOffset = 0
                    val parallax = 0.6f
                    CoverHeaderRow(
                        title = album.title, imageRequest = album.photo.mediumUrl,
                        modifier = Modifier.graphicsLayer {
                            scrolledY += listState.firstVisibleItemScrollOffset - previousOffset
                            translationY = scrolledY * parallax
                            previousOffset = listState.firstVisibleItemScrollOffset
                        }
                    )
                }

                val details by derivedStateOf { viewState.albumDetails }
                val detailsLoading = details is Incomplete

                val albumAudios = albumAudios(album, details, detailsLoading)

                albumDetailsFail(details, onRetry, maxHeight)
                albumDetailsEmpty(details, albumAudios.isEmpty(), onRetry, maxHeight)
            } else {
                item {
                    FullScreenLoading()
                }
            }
        }
    }
}

private fun LazyListScope.albumAudios(
    album: Album,
    details: Async<List<Audio>>,
    detailsLoading: Boolean,
): List<Audio> {
    val albumAudios = when (details) {
        is Success -> details()
        is Loading -> (1..album.songCount).map { Audio() }
        else -> emptyList()
    }

    if (albumAudios.isNotEmpty()) {
        item {
            Text(
                stringResource(R.string.search_audios), style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colors.background)
                    .padding(AppTheme.specs.inputPaddings)
            )
        }

        itemsIndexed(albumAudios) { index, audio ->
            val playbackConnection = LocalPlaybackConnection.current
            val coroutine = rememberCoroutineScope()
            AudioRow(audio, isPlaceholder = detailsLoading, modifier = Modifier.background(MaterialTheme.colors.background)) {
                if (details is Success)
                    coroutine.launch { playbackConnection.playAlbum(album, index) }
            }
        }
    }
    return albumAudios
}

private fun LazyListScope.albumDetailsFail(
    details: Async<List<Audio>>,
    onRetry: () -> Unit,
    maxHeight: Dp,
) {
    if (details is Fail) {
        item {
            ErrorBox(
                title = stringResource(details.error.localizedTitle()),
                message = stringResource(details.error.localizedMessage()),
                onRetryClick = onRetry,
                maxHeight = maxHeight
            )
        }
    }
}

private fun LazyListScope.albumDetailsEmpty(
    details: Async<List<Audio>>,
    albumAudiosEmpty: Boolean,
    onRetry: () -> Unit,
    maxHeight: Dp,
) {
    if (details is Success && albumAudiosEmpty) {
        item {
            EmptyErrorBox(
                onRetryClick = onRetry,
                maxHeight = maxHeight
            )
        }
    }
}
