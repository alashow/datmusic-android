/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.playback.components

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import tm.alashow.base.util.extensions.Callback
import tm.alashow.common.compose.previews.CombinedPreview
import tm.alashow.common.compose.rememberFlowWithLifecycle
import tm.alashow.datmusic.domain.CoverImageSize
import tm.alashow.datmusic.ui.playback.LocalPlaybackConnection
import tm.alashow.datmusic.ui.previews.PreviewDatmusicCore

@OptIn(ExperimentalPagerApi::class)
@Composable
fun PlaybackArtworkPagerWithNowPlayingAndControls(
    nowPlaying: MediaMetadataCompat,
    playbackState: PlaybackStateCompat,
    modifier: Modifier = Modifier,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    artworkVerticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    titleTextStyle: TextStyle = PlaybackNowPlayingDefaults.titleTextStyle,
    artistTextStyle: TextStyle = PlaybackNowPlayingDefaults.artistTextStyle,
    pagerState: PagerState = rememberPagerState(),
    onArtworkClick: Callback? = null,
    onTitleClick: () -> Unit,
    onArtistClick: () -> Unit,
) {
    ConstraintLayout(modifier = modifier.fillMaxSize()) {
        val (pager, nowPlayingControls) = createRefs()
        PlaybackPager(
            nowPlaying = nowPlaying,
            pagerState = pagerState,
            modifier = Modifier
                .constrainAs(pager) {
                    centerHorizontallyTo(parent)
                    top.linkTo(parent.top)
                    bottom.linkTo(nowPlayingControls.top)
                    height = Dimension.fillToConstraints
                },
            verticalAlignment = artworkVerticalAlignment,
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
            onTitleClick = onTitleClick,
            onArtistClick = onArtistClick,
            modifier = Modifier.constrainAs(nowPlayingControls) {
                centerHorizontallyTo(parent)
                bottom.linkTo(parent.bottom)
                height = Dimension.fillToConstraints
            }
        )
    }
}

@CombinedPreview
@Composable
private fun PlaybackArtworkPagerWithNowPlayingAndControlsPreview() = PreviewDatmusicCore {
    val playbackConnection = LocalPlaybackConnection.current
    val playbackState by rememberFlowWithLifecycle(playbackConnection.playbackState)
    val nowPlaying by rememberFlowWithLifecycle(playbackConnection.nowPlaying)
    Surface {
        PlaybackArtworkPagerWithNowPlayingAndControls(
            nowPlaying = nowPlaying,
            playbackState = playbackState,
            onTitleClick = {},
            onArtistClick = {},
        )
    }
}
