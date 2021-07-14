/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.album

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.insets.ui.Scaffold
import kotlin.math.round
import tm.alashow.base.util.extensions.localizedMessage
import tm.alashow.base.util.extensions.localizedTitle
import tm.alashow.common.compose.rememberFlowWithLifecycle
import tm.alashow.datmusic.domain.entities.Album
import tm.alashow.datmusic.domain.entities.Audio
import tm.alashow.datmusic.ui.audios.AudioRow
import tm.alashow.datmusic.ui.components.CoverHeaderDefaults
import tm.alashow.datmusic.ui.components.CoverHeaderRow
import tm.alashow.domain.models.Async
import tm.alashow.domain.models.Fail
import tm.alashow.domain.models.Incomplete
import tm.alashow.domain.models.Loading
import tm.alashow.domain.models.Success
import tm.alashow.navigation.LocalNavigator
import tm.alashow.ui.OffsetNotifyingBox
import tm.alashow.ui.components.CollapsingTopBar
import tm.alashow.ui.components.EmptyErrorBox
import tm.alashow.ui.components.ErrorBox
import tm.alashow.ui.components.FullScreenLoading
import tm.alashow.ui.theme.AppTheme

@Composable
fun AlbumDetail() {
    val navigator = LocalNavigator.current

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
            ArtistDetailList(viewState, viewModel::refresh, padding, listState)
        }
    }
}

@Composable
private fun ArtistDetailList(
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
                    CoverHeaderRow(title = album.title, imageRequest = album.photo.mediumUrl)
                }

                val details = viewState.albumDetails
                val detailsLoading = details is Incomplete

                val albumAudios = albumDetails(album, details, detailsLoading)

                albumDetailsFail(details, this, onRetry, maxHeight)
                albumDetailsEmpty(details, albumAudios, this, onRetry, maxHeight)
            } else {
                item {
                    FullScreenLoading()
                }
            }
        }
    }
}

private fun LazyListScope.albumDetails(
    album: Album,
    details: Async<List<Audio>>,
    detailsLoading: Boolean
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
                modifier = Modifier.padding(AppTheme.specs.inputPaddings)
            )
        }

        items(albumAudios) { audio ->
            AudioRow(audio, isPlaceholder = detailsLoading)
        }
    }
    return albumAudios
}

private fun albumDetailsFail(
    details: Async<List<Audio>>,
    lazyListScope: LazyListScope,
    onRetry: () -> Unit,
    maxHeight: Dp,
) {
    if (details is Fail) {
        lazyListScope.item {
            ErrorBox(
                title = stringResource(details.error.localizedTitle()),
                message = stringResource(details.error.localizedMessage()),
                onRetryClick = onRetry,
                maxHeight = maxHeight
            )
        }
    }
}

private fun albumDetailsEmpty(
    details: Async<List<Audio>>,
    albumAudios: List<Audio>,
    lazyListScope: LazyListScope,
    onRetry: () -> Unit,
    maxHeight: Dp,
) {
    if (details is Success && albumAudios.isEmpty()) {
        lazyListScope.item {
            EmptyErrorBox(
                onRetryClick = onRetry,
                maxHeight = maxHeight
            )
        }
    }
}
