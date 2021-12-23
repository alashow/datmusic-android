/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.playback.components

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import tm.alashow.base.util.extensions.Callback
import tm.alashow.datmusic.domain.CoverImageSize
import tm.alashow.datmusic.ui.playback.PlaybackViewModel

@OptIn(ExperimentalPagerApi::class)
@Composable
fun PlaybackArtworkPagerWithNowPlayingAndControls(
    nowPlaying: MediaMetadataCompat,
    playbackState: PlaybackStateCompat,
    modifier: Modifier = Modifier,
    contentColor: Color = MaterialTheme.colors.onBackground,
    titleTextStyle: TextStyle = PlaybackNowPlayingDefaults.titleTextStyle,
    artistTextStyle: TextStyle = PlaybackNowPlayingDefaults.artistTextStyle,
    pagerState: PagerState = rememberPagerState(),
    onArtworkClick: Callback? = null,
    viewModel: PlaybackViewModel = hiltViewModel(),
) {
    ConstraintLayout(modifier = modifier) {
        val (pager, nowPlayingControls) = createRefs()
        PlaybackPager(
            nowPlaying = nowPlaying,
            pagerState = pagerState,
            modifier = Modifier.constrainAs(pager) {
                centerHorizontallyTo(parent)
                top.linkTo(parent.top)
                bottom.linkTo(nowPlayingControls.top)
                height = Dimension.fillToConstraints
            }
        ) { audio, _, pagerMod ->
            val currentArtwork = audio.coverUri(CoverImageSize.LARGE)
            PlaybackArtwork(
                artwork = currentArtwork,
                contentColor = contentColor,
                nowPlaying = nowPlaying,
                onClick = onArtworkClick,
                modifier = pagerMod,
            )
        }
        PlaybackNowPlayingWithControls(
            nowPlaying = nowPlaying,
            playbackState = playbackState,
            contentColor = contentColor,
            titleTextStyle = titleTextStyle,
            artistTextStyle = artistTextStyle,
            onTitleClick = viewModel::onTitleClick,
            onArtistClick = viewModel::onArtistClick,
            modifier = Modifier.constrainAs(nowPlayingControls) {
                centerHorizontallyTo(parent)
                bottom.linkTo(parent.bottom)
                height = Dimension.fillToConstraints
            }
        )
    }
}
