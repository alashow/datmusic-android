/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.artist

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.google.accompanist.insets.ui.Scaffold
import kotlin.math.round
import tm.alashow.base.util.extensions.localizedMessage
import tm.alashow.base.util.extensions.localizedTitle
import tm.alashow.common.compose.LocalPlaybackConnection
import tm.alashow.common.compose.rememberFlowWithLifecycle
import tm.alashow.datmusic.domain.entities.Album
import tm.alashow.datmusic.domain.entities.Artist
import tm.alashow.datmusic.domain.entities.Audio
import tm.alashow.datmusic.ui.albums.AlbumColumn
import tm.alashow.datmusic.ui.audios.AudioRow
import tm.alashow.datmusic.ui.components.CoverHeaderDefaults
import tm.alashow.datmusic.ui.components.CoverHeaderRow
import tm.alashow.domain.models.Async
import tm.alashow.domain.models.Fail
import tm.alashow.domain.models.Incomplete
import tm.alashow.domain.models.Loading
import tm.alashow.domain.models.Success
import tm.alashow.navigation.LocalNavigator
import tm.alashow.navigation.Navigator
import tm.alashow.navigation.screens.LeafScreen
import tm.alashow.ui.OffsetNotifyingBox
import tm.alashow.ui.components.CollapsingTopBar
import tm.alashow.ui.components.EmptyErrorBox
import tm.alashow.ui.components.ErrorBox
import tm.alashow.ui.components.fullScreenLoading
import tm.alashow.ui.theme.AppTheme

@Composable
fun ArtistDetail(navigator: Navigator = LocalNavigator.current) {
    ArtistDetail(viewModel = hiltViewModel()) {
        navigator.goBack()
    }
}

@Composable
private fun ArtistDetail(viewModel: ArtistDetailViewModel, onBackClick: () -> Unit = {}) {
    val viewState by rememberFlowWithLifecycle(viewModel.state).collectAsState(initial = ArtistDetailViewState.Empty)
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
                    title = stringResource(R.string.artists_detail_title),
                    collapsed = headerOffsetProgress.value == 0f,
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
    viewState: ArtistDetailViewState,
    onRetry: () -> Unit,
    padding: PaddingValues = PaddingValues(),
    listState: LazyListState,
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = padding.calculateTopPadding() + padding.calculateBottomPadding())
    ) {
        val artist = viewState.artist
        if (artist != null) {
            var scrolledY = 0f
            var previousOffset = 0
            val parallax = 0.3f
            item {
                CoverHeaderRow(
                    title = artist.name,
                    imageRequest = artist.largePhoto(),
                    modifier = Modifier.graphicsLayer {
                        scrolledY += listState.firstVisibleItemScrollOffset - previousOffset
                        translationY = scrolledY * parallax
                        previousOffset = listState.firstVisibleItemScrollOffset
                    }
                )
            }

            val details = viewState.artistDetails
            val detailsLoading = details is Incomplete

            val (artistAlbums, artistAudios) = artistDetails(details, detailsLoading)

            artistDetailsFail(details, onRetry)
            artistDetailsEmpty(details, artistAlbums.isEmpty() && artistAudios.isEmpty(), onRetry)
        } else {
            fullScreenLoading()
        }
    }
}

private fun LazyListScope.artistDetails(
    details: Async<Artist>,
    detailsLoading: Boolean
): Pair<List<Album>, List<Audio>> {
    val artistAlbums = when (details) {
        is Success -> details().albums
        is Loading -> (1..5).map { Album() }
        else -> emptyList()
    }
    val artistAudios = when (details) {
        is Success -> details().audios
        is Loading -> (1..5).map { Audio() }
        else -> emptyList()
    }

    if (artistAlbums.isNotEmpty()) {
        item {
            Text(
                stringResource(R.string.search_albums), style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colors.background)
                    .padding(AppTheme.specs.inputPaddings)
            )
        }

        item {
            LazyRow(Modifier.fillMaxWidth()) {
                items(artistAlbums) { album ->
                    val navigator = LocalNavigator.current
                    AlbumColumn(album, isPlaceholder = detailsLoading, modifier = Modifier.background(MaterialTheme.colors.background)) {
                        navigator.navigate(LeafScreen.AlbumDetails.buildRoute(it))
                    }
                }
            }
        }
    }

    if (artistAudios.isNotEmpty()) {
        item {
            Text(
                stringResource(R.string.search_audios), style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colors.background)
                    .padding(AppTheme.specs.inputPaddings)
            )
        }

        itemsIndexed(artistAudios) { index, audio ->
            val playbackConnection = LocalPlaybackConnection.current
            AudioRow(
                audio = audio,
                isPlaceholder = detailsLoading,
                modifier = Modifier.background(MaterialTheme.colors.background),
                onPlayAudio = {
                    if (details is Success)
                        playbackConnection.playArtist(details(), index)
                }
            )
        }
    }
    return Pair(artistAlbums, artistAudios)
}

private fun LazyListScope.artistDetailsFail(
    details: Async<Artist>,
    onRetry: () -> Unit,
) {
    if (details is Fail) {
        item {
            ErrorBox(
                title = stringResource(details.error.localizedTitle()),
                message = stringResource(details.error.localizedMessage()),
                onRetryClick = onRetry,
                modifier = Modifier.fillParentMaxHeight()
            )
        }
    }
}

private fun LazyListScope.artistDetailsEmpty(
    details: Async<Artist>,
    detailsEmpty: Boolean,
    onRetry: () -> Unit,
) {
    if (details is Success && detailsEmpty) {
        item {
            EmptyErrorBox(
                onRetryClick = onRetry,
                modifier = Modifier.fillParentMaxHeight()
            )
        }
    }
}
