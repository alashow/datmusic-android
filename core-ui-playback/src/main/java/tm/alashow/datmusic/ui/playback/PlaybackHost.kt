/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.playback

import android.support.v4.media.session.PlaybackStateCompat.STATE_PLAYING
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import tm.alashow.base.util.toast
import tm.alashow.common.compose.LocalScaffoldState
import tm.alashow.common.compose.rememberFlowWithLifecycle
import tm.alashow.datmusic.playback.NONE_PLAYBACK_STATE
import tm.alashow.datmusic.playback.NONE_PLAYING
import tm.alashow.datmusic.playback.PlaybackConnection
import tm.alashow.datmusic.playback.artist
import tm.alashow.datmusic.playback.title
import tm.alashow.ui.theme.AppTheme
import tm.alashow.ui.theme.translucentSurface

val LocalPlaybackConnection = staticCompositionLocalOf<PlaybackConnection> {
    error("No LocalPlaybackConnection provided")
}

@Composable
fun PlaybackHost(
    viewModel: PlaybackViewModel = hiltViewModel(),
    scaffoldState: ScaffoldState = LocalScaffoldState.current,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    val playbackState by rememberFlowWithLifecycle(viewModel.playbackConnection.playbackState).collectAsState(NONE_PLAYBACK_STATE)
    LaunchedEffect(playbackState.errorCode) {
        playbackState.errorMessage?.apply { context.toast("Playback error: ${playbackState.errorMessage}") }
    }

    CompositionLocalProvider(LocalPlaybackConnection provides viewModel.playbackConnection) {
        content()
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PlaybackMiniControls() {
    val playbackState by rememberFlowWithLifecycle(LocalPlaybackConnection.current.playbackState).collectAsState(NONE_PLAYBACK_STATE)
    val nowPlaying by rememberFlowWithLifecycle(LocalPlaybackConnection.current.nowPlaying).collectAsState(NONE_PLAYING)

    val visible = playbackState.state == STATE_PLAYING
    AnimatedVisibility(visible = visible) {
        Row(
            Modifier
                .height(32.dp)
                .fillMaxWidth()
                .translucentSurface()
                .horizontalScroll(rememberScrollState())
                .padding(AppTheme.specs.paddingTiny)
        ) {
            Text("${nowPlaying.artist} - ${nowPlaying.title}")
        }
    }
}
