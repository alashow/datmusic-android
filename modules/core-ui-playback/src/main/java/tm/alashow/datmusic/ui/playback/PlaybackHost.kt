/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.playback

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.hilt.navigation.compose.hiltViewModel
import tm.alashow.datmusic.playback.PlaybackConnection

@Composable
fun PlaybackHost(content: @Composable () -> Unit) {
    PlaybackHost(
        playbackConnection = hiltViewModel<PlaybackConnectionViewModel>().playbackConnection,
        content = content,
    )
}

@Composable
private fun PlaybackHost(
    playbackConnection: PlaybackConnection,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalPlaybackConnection provides playbackConnection) { content() }
}
