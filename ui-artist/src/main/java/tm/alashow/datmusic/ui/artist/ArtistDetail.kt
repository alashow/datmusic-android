/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.artist

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
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
import tm.alashow.navigation.LeafScreen
import tm.alashow.navigation.LocalNavigator
import tm.alashow.ui.OffsetNotifyingBox
import tm.alashow.ui.components.CollapsingTopBar
import tm.alashow.ui.components.EmptyErrorBox
import tm.alashow.ui.components.ErrorBox
import tm.alashow.ui.components.ProgressIndicator
import tm.alashow.ui.theme.AppTheme

@Composable
fun ArtistDetail() {
    val navigator = LocalNavigator.current
    ArtistDetail(viewModel = hiltViewModel()) {
        navigator.back()
    }
}

@Composable
private fun ArtistDetail(viewModel: ArtistDetailViewModel, onBackClick: () -> Unit = {}) {
    val viewState by rememberFlowWithLifecycle(viewModel.state).collectAsState(initial = ArtistDetailViewState.Empty)

    val headerHeight = CoverHeaderDefaults.height
    val headerOffsetProgress = Animatable(0f)

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
            ArtistDetailList(viewState, viewModel::refresh, padding)
        }
    }
}

@Composable
private fun ArtistDetailList(
    viewState: ArtistDetailViewState,
    onRetry: () -> Unit,
    padding: PaddingValues = PaddingValues(),
    modifier: Modifier = Modifier
) {
    BoxWithConstraints {
        LazyColumn(
            state = rememberLazyListState(),
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = padding.calculateTopPadding() + padding.calculateBottomPadding())
        ) {
            val artist = viewState.artist
            if (artist != null) {
                item {
                    CoverHeaderRow(title = artist.name, imageRequest = artist.largePhoto())
                }

                val details = viewState.artistDetails
                val detailsLoading = details is Incomplete

                val (artistAlbums, artistAudios) = ArtistDetails(details, detailsLoading)

                ArtistDetailsFail(details, this, onRetry, maxHeight)
                ArtistDetailsEmpty(details, artistAlbums, artistAudios, this, onRetry, maxHeight)
            } else {
                item {
                    ArtistDetailsLoading()
                }
            }
        }
    }
}

private fun LazyListScope.ArtistDetails(
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
                modifier = Modifier.padding(AppTheme.specs.inputPaddings)
            )
        }

        item {
            LazyRow(Modifier.fillMaxWidth()) {
                items(artistAlbums) { album ->
                    val navigator = LocalNavigator.current
                    AlbumColumn(album, isPlaceholder = detailsLoading) {
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
                modifier = Modifier.padding(AppTheme.specs.inputPaddings)
            )
        }

        items(artistAudios) { audio ->
            AudioRow(audio, isPlaceholder = detailsLoading)
        }
    }
    return Pair(artistAlbums, artistAudios)
}

private fun ArtistDetailsFail(
    details: Async<Artist>,
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

private fun ArtistDetailsEmpty(
    details: Async<Artist>,
    artistAlbums: List<Album>,
    artistAudios: List<Audio>,
    lazyListScope: LazyListScope,
    onRetry: () -> Unit,
    maxHeight: Dp,
) {
    if (details is Success && artistAlbums.isEmpty() && artistAudios.isEmpty()) {
        lazyListScope.item {
            EmptyErrorBox(
                onRetryClick = onRetry,
                maxHeight = maxHeight
            )
        }
    }
}

@Composable
private fun BoxWithConstraintsScope.ArtistDetailsLoading() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(maxHeight)
    ) {
        ProgressIndicator()
    }
}
